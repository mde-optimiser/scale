package uk.ac.kcl.inf.mdeoptimiser.results.analyser.results;

import java.util.ArrayList;
import java.util.List;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ProblemConfiguration;

public class AnalyserExperiment {

  private AnalyserConfiguration configuration;
  private List<AnalyserModel> models;

  public AnalyserExperiment(ProblemConfiguration problemConfiguration, List<Model> models) {

    this.setExperimentConfiguration(problemConfiguration);
    this.setExperimentModels(models);
  }

  private void setExperimentModels(List<Model> models) {
    this.models = new ArrayList<>();
    models.forEach(model -> this.models.add(new AnalyserModel(model)));
  }

  private void setExperimentConfiguration(ProblemConfiguration configuration) {
    this.configuration = new AnalyserConfiguration(configuration);
  }
}
