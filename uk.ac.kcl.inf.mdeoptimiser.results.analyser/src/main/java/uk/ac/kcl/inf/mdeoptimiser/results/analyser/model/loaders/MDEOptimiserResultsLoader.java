package uk.ac.kcl.inf.mdeoptimiser.results.analyser.model.loaders;

import java.util.Map;
import org.moeaframework.analysis.collector.Accumulator;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Problem;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Tool;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ToolConfiguration;

public class MDEOptimiserResultsLoader implements ResultsLoader {

  private ToolConfiguration toolConfiguration;

  public MDEOptimiserResultsLoader(ToolConfiguration toolConfiguration) {

    this.toolConfiguration = toolConfiguration;
  }

  @Override
  public Map<Integer, Accumulator> loadModel(Problem problem, Model model, Tool tool) {

    return null;
  }
}
