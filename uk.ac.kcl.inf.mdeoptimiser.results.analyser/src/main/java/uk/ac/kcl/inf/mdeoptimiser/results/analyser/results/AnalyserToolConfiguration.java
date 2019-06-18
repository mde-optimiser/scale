package uk.ac.kcl.inf.mdeoptimiser.results.analyser.results;

import java.util.ArrayList;
import java.util.List;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ToolConfiguration;

public class AnalyserToolConfiguration {
  private List<AnalyserBatch> batches;

  public AnalyserToolConfiguration(ToolConfiguration configuration) {
    this.setBatches(configuration);
  }

  private void setBatches(ToolConfiguration configuration) {
    this.batches = new ArrayList<>();

    // TODO Load batches using the configuration parameter
  }
}
