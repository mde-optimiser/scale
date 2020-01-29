package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.metrics;

import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.ITool;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metric {

  private String name;
  private ITool tool;
  private Map<Integer, List<Double>> values;

  public Metric(String name, ITool tool) {
    this.name = name;
    this.tool = tool;
    this.values = new HashMap<>();
  }

  void addValue(Integer batch, List<Double> values) {

    if (this.values.containsKey(batch)) {}

    this.values.put(batch, values);
  }

  Map<Integer, List<Double>> getAllValues() {
    return this.values;
  }

  List<Double> getValue(Integer batch) {

    if (this.values.containsKey(batch)) {
      return this.values.get(batch);
    }

    throw new InvalidParameterException(
        String.format("Metric not found for batch with id: %s.", batch));
  }
}
