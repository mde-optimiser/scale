package uk.ac.kcl.inf.mdeoptimiser.results.analyser.model.loaders;

import java.util.Map;
import org.moeaframework.analysis.collector.Accumulator;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Problem;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Tool;

public interface ResultsLoader {

  /**
   * Load the generated accumulators for the configured problem, configuration, model and tool using
   * the provided parameters.
   *
   * @param problem
   * @param model
   * @param tool
   * @return map containing the parsed accumulators for each of the batches executed
   */
  Map<Integer, Accumulator> loadModel(Problem problem, Model model, Tool tool);
}
