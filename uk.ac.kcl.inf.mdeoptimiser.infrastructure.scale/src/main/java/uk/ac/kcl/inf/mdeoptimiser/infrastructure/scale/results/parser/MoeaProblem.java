package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

public class MoeaProblem extends AbstractProblem {

  private String problemName;
  private String modelName;

  public MoeaProblem(int objectives, int constraints, String name, String model) {

    super(1, objectives, constraints);
    this.problemName = name;
    this.modelName = model;
  }

  public boolean isMo() {

    return this.numberOfObjectives > 1;
  }

  public String getName() {
    return String.format("%s_%s", this.problemName, this.modelName);
  }

  public String getProblemName() {

    return this.problemName;
  }

  public String getModelName() {

    return this.modelName;
  }

  @Override
  public void evaluate(Solution solution) {
    // Do nothing
  }

  @Override
  public Solution newSolution() {

    return new Solution(this.numberOfVariables, this.numberOfObjectives, this.numberOfConstraints);
  }

  public boolean equals(MoeaProblem problem) {
    return this.problemName.equals(problem.getProblemName())
        && this.getModelName().equals(problem.getModelName())
        && this.getNumberOfObjectives() == problem.getNumberOfObjectives()
        && this.getNumberOfConstraints() == problem.getNumberOfConstraints();
  }
}
