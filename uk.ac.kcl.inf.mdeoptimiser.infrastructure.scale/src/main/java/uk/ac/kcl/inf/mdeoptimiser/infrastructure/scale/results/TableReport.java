package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.util.ReferenceSetMerger;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.ITool;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.MoeaProblem;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.reporting.GuidanceVectorsHelper;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableReport {

  public List<TableRow> rows;
  public String tableName;
  private Map<MoeaProblem, ReferenceSetMerger> referenceSetMergers;
  private String outputPrefix;
  private GuidanceVectorsHelper guidanceVectorsVector;
  private NondominatedPopulation referenceSet;
  private Map<MoeaProblem, NondominatedPopulation> problemReferenceSets;

  public TableReport(String tableName, String outputPrefix) {
    this.outputPrefix = outputPrefix;
    this.rows = new ArrayList<TableRow>();
    this.tableName = tableName;
    this.referenceSetMergers = new HashMap<>();
    this.problemReferenceSets = new HashMap<>();
  }

  public void setContributions(MoeaProblem problem, ReferenceSetMerger referenceSetMerger) {

    this.referenceSetMergers.put(problem, referenceSetMerger);
  }

  public void setGuidanceVectorsVector(GuidanceVectorsHelper guidanceVectorsVector) {

    this.guidanceVectorsVector = guidanceVectorsVector;
  }

  public void setReferenceSet(NondominatedPopulation referenceSet) {
    this.referenceSet = referenceSet;
  }

  public void save_tables() {
    saveStats();

    this.rows.stream()
        .map(row -> row.problem)
        .distinct()
        .forEach(
            problem -> {
              generateReferenceSetsPlot(problem);
              generateAccumulatorPlots(problem);
              calculateStatisticalSignificanceTests(problem);
              calculateMeanTime(problem);
            });
  }

  private String formatNumber(Number number) {
    DecimalFormat numberFormat = new DecimalFormat("#.00");

    return String.format("%.03f", number.doubleValue());
  }

  private void saveStats() {
    List<String> configurations = new ArrayList<>();
    List<String> steps = new ArrayList<>();

    List<String> valueMean = new ArrayList<>();
    List<String> valueMedian = new ArrayList<>();
    List<String> valueMax = new ArrayList<>();
    List<String> valueMin = new ArrayList<>();
    List<String> valueStdDev = new ArrayList<>();
    List<String> skewness = new ArrayList<>();
    List<String> kurtosis = new ArrayList<>();
    List<String> referenceSetSize = new ArrayList<>();
    List<String> referenceSetContributions = new ArrayList<>();
    List<String> bsrCalculations = new ArrayList<>();
    Table report = Table.create(this.tableName);

    rows.forEach(
        row -> {
          configurations.add(row.tool.getToolConfigurationPrettyName());

          // Create a column with the average steps
          steps.add(String.format("%.0f", row.averageStepStats.get("1").doubleValue()));
          //          batchesMeanMetrics.put("mean", descriptiveStatistics.getMean());
          //          batchesMeanMetrics.put("median", descriptiveStatistics.getPercentile(50));
          //          batchesMeanMetrics.put("standard-deviation",
          // descriptiveStatistics.getStandardDeviation());
          //          batchesMeanMetrics.put("max", descriptiveStatistics.getMax());
          //          batchesMeanMetrics.put("min", descriptiveStatistics.getMin());
          //          batchesMeanMetrics.put("geometric-mean",
          // descriptiveStatistics.getGeometricMean());
          //          batchesMeanMetrics.put("variance", descriptiveStatistics.getVariance());
          //          batchesMeanMetrics.put("skewness", descriptiveStatistics.getSkewness());
          //          batchesMeanMetrics.put("kurtosis", descriptiveStatistics.getKurtosis());

          // Show correct CRA Values
          if (row.tool.getProblem().getProblemName().equals("CRA_SO")) {
            valueMean.add(formatNumber(row.metrics.get("mean").doubleValue() * -1));
            valueMedian.add(formatNumber(row.metrics.get("median").doubleValue() * -1));
            valueMax.add(formatNumber(row.metrics.get("max").doubleValue() * -1));
            valueMin.add(formatNumber(row.metrics.get("min").doubleValue() * -1));
            skewness.add(formatNumber(row.metrics.get("skewness").doubleValue() * -1));
            kurtosis.add(formatNumber(row.metrics.get("kurtosis").doubleValue() * -1));
          } else {
            valueMean.add(formatNumber(row.metrics.get("mean")));
            valueMedian.add(formatNumber(row.metrics.get("median")));
            valueMax.add(formatNumber(row.metrics.get("max")));
            valueMin.add(formatNumber(row.metrics.get("min")));
            skewness.add(formatNumber(row.metrics.get("skewness")));
            kurtosis.add(formatNumber(row.metrics.get("kurtosis")));
          }

          valueStdDev.add(formatNumber(row.metrics.get("standard-deviation")));

          // Only calculate MO stats for MO problems
          if (row.tool.getProblem().isMo()) {

            referenceSetSize.add(formatNumber(getReferenceSetSize(row.tool)));

            referenceSetContributions.add(
                formatNumber(getReferenceSetContributions(row.tool).size()));

            bsrCalculations.add(formatNumber(getBsrCalculation(row.tool)));
          }
        });

    report.addColumns(
        StringColumn.create("configurations", configurations),
        StringColumn.create("steps", steps),
        StringColumn.create("metricMean", valueMean),
        StringColumn.create("metricMedian", valueMedian),
        StringColumn.create("metricMax", valueMax),
        StringColumn.create("metricMin", valueMin),
        StringColumn.create("metricStdDev", valueStdDev),
        StringColumn.create("skewness", skewness),
        StringColumn.create("kurtosis", kurtosis));

    // If stats for MO problems have been calculated, then include them in the corresponding
    // columns.
    if (referenceSetContributions.size() > 0) {
      report.addColumns(StringColumn.create("refSetSize", referenceSetSize));
      report.addColumns(StringColumn.create("refSetContributions", referenceSetContributions));
    }

    if (bsrCalculations.size() > 0) {
      report.addColumns(StringColumn.create("bsr", bsrCalculations));
    }

    try {
      String summaryReportFilename =
          String.format("%s/%s-summary-report.csv", this.outputPrefix, this.tableName);
      report.write().csv(summaryReportFilename);
      System.out.println("Summary report saved: " + summaryReportFilename);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void calculateStatisticalSignificanceTests(MoeaProblem problem) {

    List<String> toolNamesRow = new ArrayList<>();

    rows.stream()
        .filter(row -> row.problem.equals(problem))
        .forEach(
            row -> {
              toolNamesRow.add(String.format("%s P Value", row.tool.getToolConfigurationId()));
              toolNamesRow.add(String.format("%s U Value", row.tool.getToolConfigurationId()));
              toolNamesRow.add(String.format("%s Cohen D", row.tool.getToolConfigurationId()));
            });

    Table report = Table.create(this.tableName);

    // Add the first column with tool names
    report.addColumns(StringColumn.create("tools", toolNamesRow));

    // build a matrix with all the tools
    rows.stream()
        .filter(row -> row.problem.equals(problem))
        .forEach(
            row -> {
              List<String> dataColumn = new ArrayList<>();

              rows.stream()
                  .filter(r -> r.problem.equals(problem))
                  .forEach(
                      otherRow -> {
                        MannWhitneyUTest currentUTest = new MannWhitneyUTest();

                        dataColumn.add(
                            ""
                                + currentUTest.mannWhitneyUTest(
                                    row.getBatchesStats(), otherRow.getBatchesStats()));

                        dataColumn.add(
                            ""
                                + currentUTest.mannWhitneyU(
                                    row.getBatchesStats(), otherRow.getBatchesStats()));

                        dataColumn.add(
                            calculateCohenEffect(
                                row.getBatchesStats(), otherRow.getBatchesStats()));
                      });

              report.addColumns(StringColumn.create(row.tool.getToolConfigurationId(), dataColumn));
            });

    try {
      String summaryReportFilename =
          String.format(
              "%s/%s-%s-statistical-significance.csv",
              this.outputPrefix, this.tableName, problem.getName());
      report.write().csv(summaryReportFilename);
      System.out.println("Summary report saved: " + summaryReportFilename);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void calculateMeanTime(MoeaProblem problem) {

    List<String> toolNamesRow = new ArrayList<>();

    toolNamesRow.add("meanTime");
    toolNamesRow.add("medianTime");
    toolNamesRow.add("minTime");
    toolNamesRow.add("maxTime");

    Table report = Table.create(this.tableName);

    // Add the first column with tool names
    report.addColumns(StringColumn.create("timeStats", toolNamesRow));

    // build a matrix with all the tools
    rows.stream()
        .filter(row -> row.problem.equals(problem))
        .forEach(
            row -> {
              List<String> dataColumn = new ArrayList<>();

              DescriptiveStatistics timeStats = new DescriptiveStatistics();

              row.batchesElapsedTime.forEach(
                  (k, v) -> {
                    timeStats.addValue(v.doubleValue());
                  });

              dataColumn.add("" + timeStats.getMean());
              dataColumn.add("" + timeStats.getPercentile(50));
              dataColumn.add("" + timeStats.getMin());
              dataColumn.add("" + timeStats.getMax());

              report.addColumns(StringColumn.create(row.tool.getToolConfigurationId(), dataColumn));
            });

    try {
      String summaryReportFilename =
          String.format(
              "%s/%s-%s-elapsed-time-statistics.csv",
              this.outputPrefix, this.tableName, problem.getName());
      report.write().csv(summaryReportFilename);
      System.out.println("Summary report saved: " + summaryReportFilename);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public double getStandardDeviation(double[] population) {

    StandardDeviation stdDev = new StandardDeviation();
    return stdDev.evaluate(population);
  }

  public double getPopulationMean(double[] population) {

    Mean mean = new Mean();
    return mean.evaluate(population);
  }

  /**
   * Calculate Cohen's D effect https://trendingsideways.com/the-cohens-d-formula
   *
   * @param firstPopulation
   * @param secondPopulation
   * @return
   */
  public String calculateCohenEffect(double[] firstPopulation, double[] secondPopulation) {

    double firstPopStdDev = getStandardDeviation(firstPopulation);
    double secondPopStdDev = getStandardDeviation(secondPopulation);

    double firstPopulationMean = getPopulationMean(firstPopulation);
    double secondPopulationMean = getPopulationMean(secondPopulation);

    double pooledStdDev =
        Math.sqrt(((firstPopStdDev * firstPopStdDev + secondPopStdDev * secondPopStdDev) / 2));

    double cohenD = Math.abs((firstPopulationMean - secondPopulationMean) / pooledStdDev);

    if (cohenD >= 0.2d && cohenD < 0.5d) {
      return "Small";
    }

    if (cohenD >= 0.5d && cohenD < 0.8d) {
      return "Medium";
    }

    if (cohenD >= 0.8d) {
      return "Large";
    }

    return "" + cohenD;
  }

  public void generateAccumulatorPlots(MoeaProblem problem) {

    String objectiveColumnName = "median objective";

    if (problem.isMo()) {
      objectiveColumnName = "median hv";
    }

    Plot plot = new Plot();
    Map<String, Number> configurationEvolutions = new HashMap<String, Number>();

    this.rows.stream()
        .filter(row -> row.problem.equals(problem))
        .forEach(
            row -> {
              List<Number> nfe = new ArrayList<>();
              List<Double> score = new ArrayList<>();
              String tool = row.tool.getToolConfigurationId();
              row.meanStats.forEach(
                  (key, value) -> {
                    nfe.add(Integer.parseInt(key) / 100);

                    if (problem.isMo()) {
                      score.add(value.doubleValue());
                    } else {
                      score.add(value.doubleValue() * -1);
                    }
                  });

              plot.line(tool, nfe, score);
            });

    plot.setXLabel("evolutions").setYLabel(objectiveColumnName);

    File objectivesMedianPlot =
        new File(
            String.format(
                "%s/%s/plots/%s-%s.png",
                this.outputPrefix, problem.getName(), problem.getName(), "median-score-nfe"));

    objectivesMedianPlot.getParentFile().mkdirs();

    try {
      plot.save(objectivesMedianPlot);
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Statistics");
  }

  /**
   * This will generate a plot with all the reference sets from each configuration with the same
   * problem and model.
   *
   * @param problem
   */
  public void generateReferenceSetsPlot(MoeaProblem problem) {

    try {

      double[] idealPoint = new double[0];
      double[] referencePoint = new double[0];

      if (problem.isMo() && this.referenceSet != null) {
        idealPoint = guidanceVectorsVector.getIdealSolution().getObjectives();
        referencePoint = guidanceVectorsVector.getReferenceSolution().getObjectives();
      }

      Plot plot =
          new Plot()
              // Render the reference and guidance vectors points as red dots on the graph
              .scatter("Point", idealPoint, referencePoint)
              .setXLabel("Objective 1")
              .setYLabel("Objective 2")
              .setTitle(
                  String.format(
                      "Reference Sets %s %s", problem.getProblemName(), problem.getModelName()));

      this.rows.stream()
          .filter((row) -> row.problem.equals(problem))
          .forEach(
              row -> {
                plot.add(
                    row.tool.getToolConfigurationId().replace("_", " "),
                    row.tool.getBatchesReferenceSetMerger().getCombinedPopulation());
              });

      File referenceSetPlot =
          new File(
              String.format(
                  "%s/%s/plots/%s-%s.png",
                  this.outputPrefix, problem.getName(), problem.getName(), "reference-sets"));

      referenceSetPlot.getParentFile().mkdirs();

      plot.save(referenceSetPlot);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public NondominatedPopulation getReferenceSetContributions(ITool tool) {
    if (!this.referenceSetMergers.isEmpty()) {
      return this.referenceSetMergers
          .get(tool.getProblem())
          .getContributionFrom(tool.getToolConfigurationId());
    }

    return new NondominatedPopulation();
  }

  /**
   * Calculate the ratio of best solutions for the given tool's reference set vs the reference set
   * for the entire problem
   *
   * <p>//TODO I have checked that the contributions function returns the expected value even when
   * duplicate objectives are found. MoeaFramework seems to only print a duplicate errors on
   * Std.err, but the solution is still added to the pareto front for the registered source from
   * where it comes.
   *
   * @param tool
   * @return
   */
  public double getBsrCalculation(ITool tool) {

    NondominatedPopulation solutionSet = getReferenceSetContributions(tool);

    NondominatedPopulation referenceS = new NondominatedPopulation();

    // TODO Cache me
    // The reference set merger allows duplicates. The nondominatedpop does not
    referenceS.addAll(this.referenceSetMergers.get(tool.getProblem()).getCombinedPopulation());

    if (solutionSet != null) {
      return (double) solutionSet.size() / referenceS.size();
    }
    return 0d;
  }

  /** Calculate the reference set size */
  public double getReferenceSetSize(ITool tool) {

    if (!this.problemReferenceSets.containsKey(tool.getProblem())) {
      NondominatedPopulation referenceS = new NondominatedPopulation();

      // The referencesetmerger reference set merger allows duplicates. The nondominatedpop does not
      referenceS.addAll(this.referenceSetMergers.get(tool.getProblem()).getCombinedPopulation());

      this.problemReferenceSets.put(tool.getProblem(), referenceS);
    }

    return this.problemReferenceSets.get(tool.getProblem()).size();
  }
}
