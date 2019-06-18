package uk.ac.kcl.inf.mdeoptimiser.results.analyser.results;

import java.util.List;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Problem;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ProblemConfiguration;

public class AnalyserProblem {

  private Problem problem;
  private AnalyserExperiment experiment;

  public AnalyserProblem(Problem problem) {

    this.setProblem(problem);
    this.setExperiment(problem.getConfiguration(), problem.getModels());
  }

  private void setProblem(Problem problem) {
    this.problem = problem;
  }

  private void setExperiment(ProblemConfiguration problemConfiguration, List<Model> models) {
    this.experiment = new AnalyserExperiment(problemConfiguration, models);
  }

  public String getName() {
    return this.problem.getName();
  }
}
