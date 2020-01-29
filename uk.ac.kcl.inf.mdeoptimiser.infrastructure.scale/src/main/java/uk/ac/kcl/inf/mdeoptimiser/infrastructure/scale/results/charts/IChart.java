package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.charts;

public interface IChart {

  /** Sets the type of the chart. */
  void setType(ChartType type);

  void setName();

  void addTool();

  void generate();
}
