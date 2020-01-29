package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.ProblemProvider;

public class ResultsProblemProvider extends ProblemProvider {

  private final Problem problem;
  private NondominatedPopulation referenceSet;

  public ResultsProblemProvider(Problem problem, NondominatedPopulation referenceSet) {
    this.problem = problem;
    this.referenceSet = referenceSet;
  }

  @Override
  public Problem getProblem(String s) {
    if (problem.getName().equals(s)) {
      return this.problem;
    }

    return null;
  }

  @Override
  public NondominatedPopulation getReferenceSet(String s) {

    if (this.problem.getName().equals(s)) {
      return this.referenceSet;
    }

    return null;
  }
}
