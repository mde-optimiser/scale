package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results;

import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.ITool;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.MoeaProblem;

import java.util.Map;

public class TableRow {

  public Map<String, Number> batchesElapsedTime;
  public Map<String, Number> meanStats;
  public MoeaProblem problem;
  public ITool tool;
  public Map<String, Number> metrics;
  public Map<String, Number> averageStepStats;

  // This includes the HV or SO objective value across all current batches
  private Map<String, Number> batchesStats;

  public TableRow(
      MoeaProblem problem,
      ITool tool,
      Map<String, Number> metrics,
      Map<String, Number> averageStepStats,
      Map<String, Number> batchesStats,
      Map<String, Number> meanStats,
      Map<String, Number> batchesElapsedTime) {

    this.problem = problem;
    this.tool = tool;
    this.metrics = metrics;
    this.averageStepStats = averageStepStats;
    this.batchesStats = batchesStats;
    this.meanStats = meanStats;
    this.batchesElapsedTime = batchesElapsedTime;
  }

  public double[] getBatchesStats() {

    double[] population = new double[this.batchesStats.size()];

    int i = 0;

    for (String elem : this.batchesStats.keySet()) {
      population[i] = this.batchesStats.get(elem).doubleValue();
      i++;
    }
    return population;
  }
}
