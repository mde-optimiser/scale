package uk.ac.kcl.inf.mdeoptimiser.results.analyser.model.loaders;

import java.util.Map;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ToolConfiguration;

public class DynamicResultsLoader {

  private ToolConfiguration toolConfiguration;
  private Map<String, ResultsLoader> cachedLoaders;

  public DynamicResultsLoader(ToolConfiguration toolConfiguration) {

    this.toolConfiguration = toolConfiguration;
  }

  public ResultsLoader load(ToolConfiguration toolConfiguration) throws InvalidLoaderException {

    throw new InvalidLoaderException(
        "This is not implemented. Need a factory that dynamically loads"
            + "the results loaders based on the user given parameter. The loaded instances, should be cached");
  }
}
