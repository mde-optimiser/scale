package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.metrics;

import org.moeaframework.analysis.collector.Accumulator;

import java.util.Map;

public interface MetricCalculator {

  Metric calculate(Map<Integer, Accumulator> batches);
}
