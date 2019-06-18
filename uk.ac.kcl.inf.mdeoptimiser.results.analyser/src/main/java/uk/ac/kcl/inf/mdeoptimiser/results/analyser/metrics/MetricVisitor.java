package uk.ac.kcl.inf.mdeoptimiser.results.analyser.metrics;

import uk.ac.kcl.inf.mdeoptimiser.results.analyser.results.AnalyserBatch;
import uk.ac.kcl.inf.mdeoptimiser.results.analyser.results.AnalyserModel;

public abstract class MetricVisitor {

  public abstract void calculate(AnalyserModel model);

  public abstract void calculate(AnalyserBatch batch);
}
