package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider;

import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws.AWSInfrastructureProvider;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Infrastructure;

public class ProviderFactory {


  public static IProvider get(Infrastructure configuredInfrastructure) {

    switch(configuredInfrastructure.getType()) {
      //We are running this on AWS Batch
      case "aws":
        return new AWSInfrastructureProvider(configuredInfrastructure);
      default:
        throw new RuntimeException(
                String.format("Invalid infrastructure provider specified: %s", configuredInfrastructure.getType()));
    }

  }

}
