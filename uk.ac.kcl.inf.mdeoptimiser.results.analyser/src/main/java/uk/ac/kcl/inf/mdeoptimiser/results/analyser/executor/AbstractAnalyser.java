package uk.ac.kcl.inf.mdeoptimiser.results.analyser.executor;

import uk.ac.kcl.inf.mdeoptimiser.results.analyser.metrics.MetricVisitor;

public abstract class AbstractAnalyser {

  public abstract void analyse(MetricVisitor summary);
}
