package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.moeaframework.Analyzer;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.util.ReferenceSetMerger;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.reporting.GuidanceVectorsHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public abstract class AbstractParser implements ITool {

  private ReferenceSetMerger referenceSetMerger;
  protected Map<Integer, NondominatedPopulation> referenceSetCache;
  protected Map<Integer, Accumulator> accumulators;
  protected MoeaProblem problem;

  protected String experimentName;
  protected String model;
  protected String tool;


  protected AbstractParser() {
    this.referenceSetCache = new HashMap<>();
  }

  public AbstractParser(String experimentName, String model, String tool) {
    this.experimentName = experimentName;
    this.model = model;
    this.tool = tool;
    this.referenceSetCache = new HashMap<>();
  }

  public Map<Integer, Map<Integer, Double>> getBatchesSOMetrics(
          Map<Integer, Accumulator> batches, MoeaProblem problem, NondominatedPopulation referenceSet) {

    Map<Integer, Map<Integer, Double>> batchesGenerationalDistance = new TreeMap<>();

    batches.forEach(
        (id, batch) -> {
          Map<Integer, Double> generationalDistance = new TreeMap<>();

          if (problem.getNumberOfObjectives() == 1) {
            for (int i = 0; i < batch.size("NFE"); i++) {

              NondominatedPopulation currentSet =
                  new NondominatedPopulation(
                      (ArrayList<Solution>) batch.get("Approximation Set", i));

              generationalDistance.put(
                  (Integer) batch.get("NFE", i), getCurrentBestOnjectiveValue(currentSet));
            }
          }

          batchesGenerationalDistance.put(id, generationalDistance);
        });

    return batchesGenerationalDistance;
  }

  public Map<String, Number> getBatchesMOHVStats(
      Map<Integer, Accumulator> batches,
      MoeaProblem problem,
      NondominatedPopulation referenceSet,
      GuidanceVectorsHelper guidanceVectorsHelper) {

    // batch, hv
    Map<String, Number> batchesStats = new TreeMap<>();

    System.out.println(problem.getName());

    // The reference set is obtained from the problem provider
    Analyzer analyzer =
        new Analyzer()
            .withProblem(problem.getName())
            .withIdealPoint(guidanceVectorsHelper.getIdealSolution().getObjectives())
            .withReferencePoint(guidanceVectorsHelper.getReferenceSolution().getObjectives())
            .includeHypervolume();

    batches.forEach(
        (id, batch) -> {
          if (referenceSet.size() > 0) {

            NondominatedPopulation currentApproximationSet =
                new NondominatedPopulation(
                    (ArrayList<Solution>) batch.get("Approximation Set", batch.size("NFE") - 1));

            // Do we have more than one solution?
            if (currentApproximationSet.size() > 0) {
              analyzer.add(Integer.toString(id), currentApproximationSet);
            } else {
              // Cases where there is no solution
              batchesStats.put(id.toString(), 0d);
            }

          } else {
            batchesStats.put(id.toString(), 0d);
          }
        });

    Analyzer.AnalyzerResults analysis = analyzer.getAnalysis();

    batches.forEach(
        (id, batch) -> {
          if (!batchesStats.containsKey(id.toString())) {
            batchesStats.put(
                id.toString(), analysis.get(id.toString()).get("Hypervolume").getValues()[0]);
          }
        });

    return batchesStats;
  }

  public Map<String, Number> getBatchesMOStepsStats(
          Map<Integer, Accumulator> batches, MoeaProblem problem, NondominatedPopulation referenceSet) {

    // batch, hv, iterations
    Map<String, Number> batchesStats = new TreeMap<>();

    batches.forEach(
        (id, batch) -> {
          batchesStats.put(id.toString(), (double) batch.size("NFE"));
        });

    return batchesStats;
  }

  public Map<String, Number> getBatchesAverageStepsStats(
          Map<Integer, Accumulator> batches, MoeaProblem problem, NondominatedPopulation referenceSet) {

    // batch, hv, iterations
    Map<String, Number> batchesStats = new TreeMap<>();

    int total_steps = 0;

    for (Integer batch : batches.keySet()) {
      total_steps += batches.get(batch).size("NFE");
    }

    batchesStats.put("1", (double) total_steps / batches.keySet().size());

    return batchesStats;
  }

  public Map<Integer, Map<Integer, Map<String, Double>>> getBatchesMOMetrics(
      Map<Integer, Accumulator> batches,
      MoeaProblem problem,
      NondominatedPopulation problemReferenceSet,
      GuidanceVectorsHelper guidanceVectorsHelper) {

    // batch, nfe, metrics, value
    Map<Integer, Map<Integer, Map<String, Double>>> batchesMetrics = new TreeMap<>();

    batches.forEach(
        (id, batch) -> {
          Map<Integer, Map<String, Double>> qualityMetrics = new TreeMap<>();

          if (problemReferenceSet.size() > 0) {

            for (int i = 0; i < batch.size("NFE"); i++) {

              Analyzer analyzer =
                  new Analyzer()
                      .withProblem(problem.getName())
                      .withIdealPoint(guidanceVectorsHelper.getIdealSolution().getObjectives())
                      .withReferencePoint(
                          guidanceVectorsHelper.getReferenceSolution().getObjectives())
                      .includeHypervolume()
                      .includeContribution();

              Map<String, Double> metrics = new HashMap<>();
              metrics.put("HV", 0d);
              metrics.put("Contribution", 0d);

              NondominatedPopulation currentApproximationSet = new NondominatedPopulation();
              currentApproximationSet.addAll(
                  (ArrayList<Solution>) batch.get("Approximation Set", i));

              String batchId = String.format("NFE_%s", batch.get("NFE", i));

              analyzer.add(batchId, currentApproximationSet);

              Analyzer.AnalyzerResults result = analyzer.getAnalysis();

              if (result.get(batchId).get("Hypervolume").getValues().length > 0) {
                metrics.put("HV", result.get(batchId).get("Hypervolume").getValues()[0]);
              }

              if (result.get(batchId).get("Contribution").getValues().length > 0) {
                metrics.put("Contribution", result.get(batchId).get("Contribution").getValues()[0]);
              }

              qualityMetrics.put((Integer) batch.get("NFE", i), metrics);
            }

          } else {

            for (int i = 0; i < batch.size("NFE"); i++) {
              Map<String, Double> metrics = new HashMap<>();
              metrics.put("HV", 0d);
              metrics.put("Contribution", 0d);
              qualityMetrics.put((Integer) batch.get("NFE", i), metrics);
            }
          }

          batchesMetrics.put(id, qualityMetrics);
        });

    return batchesMetrics;
  }

  public double calculateSODistance(NondominatedPopulation best, NondominatedPopulation current) {

    double bestValue = best.get(0).getObjective(0);
    double currentValue = current.get(0).getObjective(0);

    return (Math.abs(bestValue) - Math.abs(currentValue));
  }

  /**
   * Returns the best objective value from a SO reference set
   *
   * @param current
   * @return
   */
  public double getCurrentBestOnjectiveValue(NondominatedPopulation current) {

    if (current.size() == 0) {
      System.out.println("Current reference set size: " + current.size());
      throw new IllegalArgumentException("SO Problems should not be empty.");
    }

    // We have no valid solutions in this reference set, so return 0
    if (current.size() > 1) {
      return 0;
    }

    Solution solution = current.get(0);

    // Return 0 while the solution is invalid
    if (solution.violatesConstraints()) {
      return 0;
    }

    return solution.getObjective(0);
  }

  public NondominatedPopulation getReferenceSet(Accumulator accumulator) {

    if (this.referenceSetCache.containsKey(accumulator.hashCode())) {
      return this.referenceSetCache.get(accumulator.hashCode());
    }

    NondominatedPopulation referenceSet = new NondominatedPopulation();

    String approximationSetKey = "Approximation Set";

    for (int i = 0; i < accumulator.size(approximationSetKey); i++) {
      referenceSet.addAll((List<Solution>) accumulator.get(approximationSetKey, i));
    }

    this.referenceSetCache.put(accumulator.hashCode(), referenceSet);

    return referenceSet;
  }

  public Map<Integer, NondominatedPopulation> getNondominatedSets() {

    Map<Integer, NondominatedPopulation> nonDominatedSets =
        new TreeMap<Integer, NondominatedPopulation>();

    accumulators.forEach(
        (id, accumulator) -> nonDominatedSets.put(id, getReferenceSet(accumulator)));

    return nonDominatedSets;
  }

  public Map<String, Number> medianBatchMetrics(Map<Integer, Map<Integer, Double>> batches) {

    Map<String, Number> batchesMeanMetrics = new TreeMap<>();

    Map<Integer, Double> highestNfeBatch = null;

    for (Integer batchId : batches.keySet()) {
      if (highestNfeBatch == null) {
        highestNfeBatch = batches.get(batchId);
      } else if (highestNfeBatch.keySet().size() < batches.get(batchId).keySet().size()) {
        highestNfeBatch = batches.get(batchId);
      }
    }

    // Find batch with highest NFE to carry the others up
    highestNfeBatch.forEach(
        (nfe, value) -> {
          DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();

          batches.forEach(
              (otherId, otherBatch) -> {
                if (otherBatch.containsKey(nfe)) {
                  descriptiveStatistics.addValue(otherBatch.get(nfe));
                } else {
                  descriptiveStatistics.addValue(
                      otherBatch.get(
                          otherBatch.keySet().toArray()[otherBatch.keySet().size() - 1]));
                }
              });

          batchesMeanMetrics.put(nfe.toString(), descriptiveStatistics.getPercentile(50));
        });

    return batchesMeanMetrics;
  }

  public Map<String, Number> unaryValueStats(Map<String, Number> batches) {

    Map<String, Number> batchesMeanMetrics = new HashMap<>();
    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();

    batches.forEach(
        (id, batch) -> {
          descriptiveStatistics.addValue(batch.doubleValue());
        });

    batchesMeanMetrics.put("mean", descriptiveStatistics.getMean());
    batchesMeanMetrics.put("median", descriptiveStatistics.getPercentile(50));
    batchesMeanMetrics.put("standard-deviation", descriptiveStatistics.getStandardDeviation());
    batchesMeanMetrics.put("max", descriptiveStatistics.getMax());
    batchesMeanMetrics.put("min", descriptiveStatistics.getMin());
    batchesMeanMetrics.put("geometric-mean", descriptiveStatistics.getGeometricMean());
    batchesMeanMetrics.put("variance", descriptiveStatistics.getVariance());
    batchesMeanMetrics.put("skewness", descriptiveStatistics.getSkewness());
    batchesMeanMetrics.put("kurtosis", descriptiveStatistics.getKurtosis());

    return batchesMeanMetrics;
  }

  public boolean hasAccumulators() {

    if (this.accumulators == null) {
      this.getAccumulators();
    }

    return !this.accumulators.isEmpty();
  }

  public Map<Integer, Accumulator> getAccumulators() {

    if (this.accumulators == null) {
      try {
        this.loadAccumulators();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return this.accumulators;
  }

  public void loadAccumulators() throws IOException {
    File root = this.getExperimentsPath();

    if (!root.exists()) {
      System.out.println("Could not find accumulators for path: " + root.getAbsolutePath());

      this.accumulators = new HashMap<>();

      return;
    }

    Map<Integer, String> paths = new HashMap<>();

    Files.walk(root.toPath())
        .filter(path -> !Files.isDirectory(path) && path.toString().endsWith("csv"))
        .forEach(
            path -> {
              int batchId = Integer.parseInt(path.getFileName().toString().split("_")[1]);

              paths.put(batchId, path.toAbsolutePath().toString());
            });

    Map<Integer, Accumulator> accumulators = new HashMap<Integer, Accumulator>();

    paths.forEach((id, path) -> accumulators.put(id, parseSerialisedBatchAccumulator(path)));

    this.accumulators = accumulators;
  }

  public String getToolConfigurationId() {
    return String.format(
        "%s-%s-%s", getUniqueName(), getProblem().getProblemName(), getProblem().getModelName());
  }

  public String getToolConfigurationPrettyName() {
    return String.format(
        "%s %s",
        getUniqueName(), getProblem().getModelName().replace("_", " ").replace("Model ", ""));
  }

  public ReferenceSetMerger getBatchesReferenceSetMerger() {
    if (this.referenceSetMerger == null) {

      this.referenceSetMerger = new ReferenceSetMerger();

      getAccumulators()
          .forEach(
              (batchId, acc) -> {

                // Register the contribution to the reference set of each tools' batch
                referenceSetMerger.add(
                    String.format("%s_batch_%s", getToolConfigurationId(), batchId),
                    getReferenceSet(acc));
              });
    }

    return this.referenceSetMerger;
  }

  public Map<? extends String, ? extends Number> getBatchesElapsedTime(
      Map<Integer, Accumulator> accumulators) {

    Map<String, Number> elapsedTime = new TreeMap<>();

    accumulators.forEach(
        (key, accumulator) -> {
          Number time =
              (Number) accumulator.get("Elapsed Time", accumulator.size("Elapsed Time") - 1);

          elapsedTime.put(key.toString(), time);
        });

    return elapsedTime;
  }
}
