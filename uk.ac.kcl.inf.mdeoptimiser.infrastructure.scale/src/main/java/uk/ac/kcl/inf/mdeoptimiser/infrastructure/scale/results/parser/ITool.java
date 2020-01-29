package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser;

import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.util.ReferenceSetMerger;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.reporting.GuidanceVectorsHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface ITool {

  /**
   * Extract one accumulator for each algorithm step from the serialised algorithm output.
   *
   * @throws IOException
   */
  void loadAccumulators() throws IOException;

  /**
   * Check if for this tool there is a configuration for the specified problem.
   *
   * @return boolean to indicate if the tool has any results
   */
  boolean hasAccumulators();

  String getToolConfigurationId();

  String getToolConfigurationPrettyName();

  Map<Integer, Accumulator> getAccumulators();

  /**
   * Extract the algorithm archive for each algorithm step
   *
   * @return
   */
  Map<Integer, NondominatedPopulation> getNondominatedSets();

  /**
   * Calculate MO(HV, GD, IGD) metrics for each batch.
   *
   * @param batches
   * @param problem
   * @param referenceSet
   * @return
   */
  Map<Integer, Map<Integer, Map<String, Double>>> getBatchesMOMetrics(
          Map<Integer, Accumulator> batches,
          MoeaProblem problem,
          NondominatedPopulation referenceSet,
          GuidanceVectorsHelper guidanceVectorsHelper);

  /**
   * Calculate HV for the pareto front found by each individual batch.
   *
   * @param batches
   * @param problem
   * @param referenceSet
   * @param guidanceVectorsHelper
   * @return
   */
  Map<String, Number> getBatchesMOHVStats(
          Map<Integer, Accumulator> batches,
          MoeaProblem problem,
          NondominatedPopulation referenceSet,
          GuidanceVectorsHelper guidanceVectorsHelper);

  /**
   * Calculate the NFE and number of steps for each batch.
   *
   * @param batches
   * @param problem
   * @param referenceSet
   * @return
   */
  Map<String, Number> getBatchesMOStepsStats(
          Map<Integer, Accumulator> batches, MoeaProblem problem, NondominatedPopulation referenceSet);

  /**
   * Calculate average NFE for each configuration across all batches.
   *
   * @param batches
   * @param problem
   * @param referenceSet
   * @return
   */
  Map<String, Number> getBatchesAverageStepsStats(
          Map<Integer, Accumulator> batches, MoeaProblem problem, NondominatedPopulation referenceSet);

  /**
   * Calculate generational distance for the objective values in SO configurations.
   *
   * @param batches
   * @param problem
   * @param referenceSet
   * @return
   */
  Map<Integer, Map<Integer, Double>> getBatchesSOMetrics(
          Map<Integer, Accumulator> batches, MoeaProblem problem, NondominatedPopulation referenceSet);

  /**
   * Calculate averages for all calculated batch metrics.
   *
   * @param batches
   * @return
   */
  Map<String, Number> medianBatchMetrics(Map<Integer, Map<Integer, Double>> batches);

  /**
   * Get the currently configured problem for which metrics are being calculated.
   *
   * @return problem instance
   */
  MoeaProblem getProblem();

  /**
   * Get current tool name.
   *
   * @return
   */
  String getUniqueName();

  /**
   * Get experiments folder prefix.
   *
   * @return
   */
  String getExperimentName();

  String getModel();

  String getTool();

  Accumulator parseSerialisedBatchAccumulator(String batchCSV);

  File getExperimentsPath();

  Map<String, Number> unaryValueStats(Map<String, Number> meanBatchMetrics);

  ReferenceSetMerger getBatchesReferenceSetMerger();

  Map<? extends String, ? extends Number> getBatchesElapsedTime(
          Map<Integer, Accumulator> accumulators);
}
