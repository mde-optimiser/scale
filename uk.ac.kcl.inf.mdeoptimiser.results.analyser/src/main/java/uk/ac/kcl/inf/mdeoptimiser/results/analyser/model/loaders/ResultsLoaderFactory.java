package uk.ac.kcl.inf.mdeoptimiser.results.analyser.model.loaders;

import com.google.inject.Inject;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ToolConfiguration;

public class ResultsLoaderFactory {

  @Inject private DynamicResultsLoader dynamicResultsLoader;

  public ResultsLoader getLoader(ToolConfiguration toolConfiguration)
      throws InvalidLoaderException {

    switch (toolConfiguration.getName()) {
      case "MDEO":
        return new MDEOptimiserResultsLoader(toolConfiguration);

      default:
        return dynamicResultsLoader.load(toolConfiguration);
    }
  }
}
