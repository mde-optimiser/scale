package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results;

import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.*;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.util.ReferenceSetMerger;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.ResultsProblemProvider;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.ITool;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.MoeaProblem;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.reporting.GuidanceVectorsHelper;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.util.StringUtils;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ResultsAnalyser {

  private static String outputPrefix = "experiment-results/output-data";
  private List<MoeaProblem> problems;
  private List<ITool> tools;

  public ResultsAnalyser(){
    this.problems = new ArrayList<MoeaProblem>();
  }

  /**
   * Run the results analysis for the configured experiments
   * @param experimentInstanceName
   * @param jobs
   * @return
   */
  public String runResultsAnalysis(String experimentInstanceName, List<ScaleJob> jobs) {

    this.tools = this.loadTools(experimentInstanceName, jobs);

    for(var problem : this.problems){

      if(problem.isMo()){
        this.runAnalysis(this.problems);
      } else {
        this.runSOAnalysis(this.problems);
      }
    }

    return this.outputPrefix;
  }

  /**
   * Load the tools and the generated results for the models configured in this case study
   * @param experimentInstanceName
   * @param jobs
   * @return list of tool result parsers
   */
  public List<ITool> loadTools(String experimentInstanceName, List<ScaleJob> jobs){
    List<String> models = jobs.stream().map(job -> job.getTask().getModelName()).distinct().collect(Collectors.toList());
    List<String> tools = jobs.stream().map(job -> job.getTask().getName()).distinct().collect(Collectors.toList());
    List<ITool> toolResults = new ArrayList<ITool>();

    MoeaProblem problem =  null;
    for(var model : models) {
      for(var tool : tools) {

        var toolParser = new JobResultsParser(experimentInstanceName, model, tool);
        if(problem == null){
          problem = toolParser.getProblem();
        } else {
          toolParser.setProblem(problem);
        }

        toolResults.add(toolParser);
      }
      this.problems.add(problem);
      problem = null;
    }

    return toolResults;
  }

  public List<ITool> getProblemTools(MoeaProblem problem){
    return this.tools.stream()
            .filter(t -> t.getProblem().equals(problem))
            .collect(Collectors.toList());
  }

  public void runSOAnalysis(List<MoeaProblem> problems) {

    TableReport tableReport = new TableReport("SO Summary", this.outputPrefix);

    // Run analysis for SO problems
    problems.stream()
        .filter(problem -> !problem.isMo())
        .forEach(
            problem -> {
              System.out.println(String.format("Parsing problem: %s", problem.getProblemName()));

              // OM stands for ohne mutation, NB stands for no breeding
              List<ITool> so_tools = this.getProblemTools(problem);

              // Calculate metrics
              ReferenceSetMerger referenceSetMerger = calculateReferenceSet(so_tools);

              savePopulation(problem, referenceSetMerger.getCombinedPopulation(), "so-refset");

              ProblemFactory.getInstance()
                  .addProvider(
                      new ResultsProblemProvider(
                          problem, referenceSetMerger.getCombinedPopulation()));

              so_tools.stream()
                  .filter(tool -> tool.getProblem().equals(problem))
                  .forEach(
                      tool -> {
                        System.out.println(
                            "Processing SO "
                                + tool.getUniqueName()
                                + " "
                                + tool.getProblem().getProblemName());

                        if (tool.hasAccumulators()) {
                          calculateMetricsSO(
                              tableReport,
                              tool,
                              tool.getProblem().getProblemName(),
                              tool.getProblem().getModelName(),
                              tool.getAccumulators(),
                              referenceSetMerger.getCombinedPopulation());
                          tool.getAccumulators().clear();
                        }
                      });
            });

    tableReport.save_tables();

    System.out.println("Completed SO");
  }

  public void runAnalysis(List<MoeaProblem> problems) {

    TableReport tableReport = new TableReport("MO Summary", this.outputPrefix);

    // Run analysis for MO problems
    problems.stream()
        .filter(problem -> problem.isMo())
        .forEach(
            problem -> {
              List<ITool> mo_tools = this.getProblemTools(problem);

              ReferenceSetMerger referenceSetMerger = calculateReferenceSet(mo_tools);

              // Calculate metrics
              NondominatedPopulation referenceSet = referenceSetMerger.getCombinedPopulation();

              GuidanceVectorsHelper guidanceVectorsHelper =
                  calculateGuidanceVectors(problem, mo_tools);

              Population guidanceVectors = new Population();
              guidanceVectors.add(guidanceVectorsHelper.getIdealSolution());
              guidanceVectors.add(guidanceVectorsHelper.getReferenceSolution());

              savePopulation(problem, guidanceVectors, "mo-guidance-vectors");
              savePopulation(problem, referenceSet, "mo-reference-set");

              // Save the reference set merger for detailed reference set reporting
              tableReport.setContributions(problem, referenceSetMerger);

              ProblemFactory.getInstance()
                  .addProvider(new ResultsProblemProvider(problem, referenceSet));

              mo_tools.stream()
                  .filter(tool -> tool.getProblem().equals(problem))
                  .forEach(
                      tool -> {
                        try {
                          System.out.println(
                              "Processing MO "
                                  + tool.getUniqueName()
                                  + " "
                                  + tool.getProblem().getProblemName());

                          calculateMetricsMO(
                              tableReport, tool, referenceSet, guidanceVectorsHelper);
                          tool.getAccumulators().clear();
                        } catch (Exception e) {
                          System.out.println(
                              "Exception: "
                                  + tool.getProblem().getProblemName()
                                  + " "
                                  + tool.getProblem().getModelName());
                          throw e;
                        }
                      });
            });

    tableReport.save_tables();

    System.out.println("Completed MO");
  }

  /**
   * Calculate the reference set contribution per tool, for each batch and for per tool for the
   * current problem configuration
   *
   * @param tools
   * @return
   */
  public ReferenceSetMerger calculateReferenceSet(List<ITool> tools) {

    NondominatedPopulation combinedPopulation =
        new NondominatedPopulation(NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES);

    ReferenceSetMerger referenceSetMerger = new ReferenceSetMerger(combinedPopulation);

    try {
      tools.forEach(
          tool -> {
            referenceSetMerger.add(
                tool.getToolConfigurationId(),
                tool.getBatchesReferenceSetMerger().getCombinedPopulation());
          });
    } catch (Exception e) {
      System.out.println(e);
    }
    return referenceSetMerger;
  }

  /**
   * Get the reference set from an accumulator by returning the Approximation Set in the last
   * algorithm step.
   *
   * @param accumulator
   * @return
   */
  public NondominatedPopulation getReferenceSet(Accumulator accumulator) {

    NondominatedPopulation referenceSet = new NondominatedPopulation();

    String approximationSetKey = "Approximation Set";

    referenceSet.addAll(
        (List<Solution>)
            accumulator.get(approximationSetKey, accumulator.size(approximationSetKey) - 1));

    return referenceSet;
  }

  public GuidanceVectorsHelper calculateGuidanceVectors(Problem problem, List<ITool> tools) {

    GuidanceVectorsHelper guidanceVectorsHelper = new GuidanceVectorsHelper(problem);

    String approximationSetKey = "Approximation Set";

    tools.stream()
        .filter(tool -> tool.hasAccumulators())
        .forEach(
            (tool) ->
                tool.getAccumulators()
                    .forEach(
                        (batchId, accumulator) -> {
                          if (accumulator != null) {
                            for (int i = 0; i < accumulator.size(approximationSetKey); i++) {
                              guidanceVectorsHelper.considerAll(
                                  (List<Solution>) accumulator.get(approximationSetKey, i));
                            }
                          }
                        }));

    return guidanceVectorsHelper;
  }

  /**
   * Calculate and save SO run results
   *
   * @param report
   * @param tool
   * @param problemName
   * @param model
   * @param results
   * @param referenceSet
   */
  public void calculateMetricsSO(
      TableReport report,
      ITool tool,
      String problemName,
      String model,
      Map<Integer, Accumulator> results,
      NondominatedPopulation referenceSet) {

    System.out.println(String.format("Processing %s model metrics", model));

    Map<String, Number> bestSolutions = new TreeMap<>();

    tool.getNondominatedSets()
        .forEach(
            (batchId, solutionSet) -> {
              bestSolutions.put(batchId.toString(), solutionSet.get(0).getObjective(0));
            });

    Map<String, Number> bestSolutionsStats = tool.unaryValueStats(bestSolutions);

    Map<String, Number> averageStepsStats =
        tool.getBatchesAverageStepsStats(results, tool.getProblem(), referenceSet);

    // Batch nfe objective
    Map<Integer, Map<Integer, Double>> batchNfeObjective = new TreeMap<>();

    batchNfeObjective.putAll(
        tool.getBatchesSOMetrics(tool.getAccumulators(), tool.getProblem(), referenceSet));

    Map<String, Number> batchesElapsedTime = new TreeMap<>();

    batchesElapsedTime.putAll(tool.getBatchesElapsedTime(tool.getAccumulators()));

    Map<String, Number> meanObjective =
        new TreeMap<>(
            (o1, o2) -> {
              Integer left = Integer.parseInt(o1);
              Integer right = Integer.parseInt(o2);
              return left.compareTo(right);
            });

    meanObjective.putAll(tool.medianBatchMetrics(batchNfeObjective));

    saveMapToCsv(tool, meanObjective, "median-objective-nfe");

    saveMapToCsv(tool, averageStepsStats, "average-steps-stats");

    saveMapToCsv(tool, bestSolutions, "best-solutions");

    saveMapToCsv(tool, batchesElapsedTime, "elapsed-time");

    results.clear();
    System.out.println(
        String.format(
            "Processed problem %s model %s for tool %s", problemName, model, tool.getUniqueName()));

    report.rows.add(
        new TableRow(
            tool.getProblem(),
            tool,
            bestSolutionsStats,
            averageStepsStats,
            bestSolutions,
            meanObjective,
            batchesElapsedTime));
  }

  /**
   * Calculate and save MO run results
   *
   * @param report
   * @param tool
   * @param referenceSet
   * @param guidanceVectorsHelper
   */
  public void calculateMetricsMO(
      TableReport report,
      ITool tool,
      NondominatedPopulation referenceSet,
      GuidanceVectorsHelper guidanceVectorsHelper) {

    if (tool.getAccumulators().isEmpty()) {
      System.out.println(
          String.format(
              "Skipping generation of results files for tool %s because there are no results parsed.",
              tool.getUniqueName()));
      return;
    }

    System.out.println("Processing tool: " + tool.getUniqueName());

    // batch, nfe, metric, value
    Map<Integer, Map<Integer, Map<String, Double>>> moMetrics = new TreeMap<>();

    moMetrics.putAll(
        tool.getBatchesMOMetrics(
            tool.getAccumulators(), tool.getProblem(), referenceSet, guidanceVectorsHelper));

    Map<Integer, Map<Integer, Double>> hypervolume = new TreeMap<>();

    moMetrics.forEach(
        (batch, runStepsMap) -> {
          Map<Integer, Double> batchHv = new TreeMap<>();

          runStepsMap.forEach(
              (nfe, metric) -> {
                batchHv.put(nfe, metric.get("HV"));
              });

          hypervolume.put(batch, batchHv);
        });

    Map<String, Number> meanHv =
        new TreeMap<>(
            (o1, o2) -> {
              Integer left = Integer.parseInt(o1);
              Integer right = Integer.parseInt(o2);
              return left.compareTo(right);
            });

    meanHv.putAll(tool.medianBatchMetrics(hypervolume));

    Map<String, Number> batchesHV =
        tool.getBatchesMOHVStats(
            tool.getAccumulators(), tool.getProblem(), referenceSet, guidanceVectorsHelper);

    Map<String, Number> hvStats = tool.unaryValueStats(batchesHV);
    Map<String, Number> averageStepStats =
        tool.getBatchesAverageStepsStats(tool.getAccumulators(), tool.getProblem(), referenceSet);

    Map<String, Number> batchesElapsedTime = new TreeMap<>();

    batchesElapsedTime.putAll(tool.getBatchesElapsedTime(tool.getAccumulators()));

    saveToolReferenceSet(tool);

    saveMapToCsv(tool, hvStats, "experiment-hv-stats");

    saveMapToCsv(tool, meanHv, "median-hv");

    saveMapToCsv(tool, batchesHV, "hv-stats");

    saveMapToCsv(
        tool,
        tool.getBatchesMOStepsStats(tool.getAccumulators(), tool.getProblem(), referenceSet),
        "steps-stats");

    saveMapToCsv(tool, averageStepStats, "avg-steps-stats");

    saveMapToCsv(tool, batchesElapsedTime, "elapsed-time");

    // Add the stats to the table report
    report.rows.add(
        new TableRow(
            tool.getProblem(),
            tool,
            hvStats,
            averageStepStats,
            batchesHV,
            meanHv,
            batchesElapsedTime));
  }

  /**
   * Saves a map of key value results to a CSV file. Keys are sorted numerically if they are numbers
   *
   * @param tool
   * @param results
   * @param tableName
   */
  public void saveMapToCsv(ITool tool, Map<String, Number> results, String tableName) {

    List<String> keys = new ArrayList<>();
    List<Number> values = new ArrayList<>();

    Map<String, Number> sortedMap = sortResultsMap(results);

    Table report = Table.create(tableName);

    sortedMap
        .keySet()
        .forEach(
            (key) -> {
              keys.add(key);
              values.add(results.get(key));
            });

    report.addColumns(StringColumn.create("row", keys), DoubleColumn.create("values", values));

    try {
      report
          .write()
          .csv(
              String.format(
                  "%s/%s-%s.csv",
                  getConfigurationPrefix(tool), getToolConfigurationName(tool), tableName));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sort key strings numerically instead of alphabetically
   *
   * @param map
   * @return
   */
  public Map<String, Number> sortResultsMap(Map<String, Number> map) {
    Map<String, Number> sortedMap =
        new TreeMap<>(
            (first, second) -> {
              if (StringUtils.isNumeric(first)) {
                Integer f = Integer.parseInt(first);
                Integer s = Integer.parseInt(second);

                int comparison = s.compareTo(f);

                if (comparison > 0) {
                  return -1;
                } else if (comparison < 0) {
                  return 1;
                }

                return 0;
              }

              return first.compareTo(second);
            });

    sortedMap.putAll(map);

    return sortedMap;
  }

  /**
   * Build a configuration prefix to use when saving output files to disk.
   *
   * @param tool
   * @return
   */
  public String getConfigurationPrefix(ITool tool) {

    File prefixPath =
        new File(
            String.format(
                "%s/%s_%s",
                this.outputPrefix,
                tool.getProblem().getProblemName(),
                tool.getProblem().getModelName()));

    prefixPath.mkdirs();

    return prefixPath.getAbsolutePath();
  }

  public String getToolConfigurationName(ITool tool) {
    return String.format(
        "%s-%s-%s",
        tool.getUniqueName(), tool.getProblem().getProblemName(), tool.getProblem().getModelName());
  }

  /**
   * Save a population object in the configured output location
   *
   * @param problem
   * @param referenceSet
   * @param populationIdentifier
   */
  public void savePopulation(
          MoeaProblem problem, Population referenceSet, String populationIdentifier) {
    try {

      String outputFilename =
          String.format(
              "%s-%s-%s", problem.getProblemName(), problem.getModelName(), populationIdentifier);

      File outputFile =
          new File(
              String.format("%s/%s/%s.csv", this.outputPrefix, problem.getName(), outputFilename));

      outputFile.getParentFile().mkdirs();

      PopulationIO.writeObjectives(outputFile, referenceSet);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void saveToolReferenceSet(ITool tool) {
    NondominatedPopulation configurationReferenceSet =
        tool.getBatchesReferenceSetMerger().getCombinedPopulation();
    try {
      String outputFilename = String.format("%s-reference-set.csv", tool.getToolConfigurationId());

      File outputFile =
          new File(
              String.format(
                  "%s/%s/reference-sets/%s.csv",
                  this.outputPrefix, tool.getProblem().getName(), outputFilename));

      outputFile.getParentFile().mkdirs();

      PopulationIO.writeObjectives(outputFile, configurationReferenceSet);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
