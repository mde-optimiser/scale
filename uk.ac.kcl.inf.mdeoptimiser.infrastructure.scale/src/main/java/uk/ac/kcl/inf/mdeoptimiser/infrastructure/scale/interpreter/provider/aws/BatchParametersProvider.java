package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Infrastructure;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Parameters generation class for the AWS Batch provider.
 *
 * Loads the user provided parameters from the DSL and generates additional parameters required for
 * initialising the architecture by querying the AWS API.
 */
public class BatchParametersProvider {

  private AmazonEC2 defaultClient;
  private Vpc defaultVpc;
  private Infrastructure configuredInfrastructure;
  private AWSBatch batchClient;
  private AmazonS3 s3Client;
  private String computeEnvironmentName;

  public BatchParametersProvider(Infrastructure configuredInfrastructure) {

    this.configuredInfrastructure = configuredInfrastructure;
  }

  /**
   * This defaults to the default configured region.
   * This reads the default region configured for the server.
   * @return instance of the default EC2 Client
   */
  public AmazonEC2 getAmazonClient(){

    if(this.defaultClient == null){
      var client = AmazonEC2ClientBuilder.defaultClient();
      this.defaultClient = client;
    }

    return this.defaultClient;
  }

  /**
   * This defaults to the default configured region.
   * @return
   */
  public AWSBatch getBatchClient() {

    if(this.batchClient == null) {
      this.batchClient = AWSBatchClientBuilder.standard().build();
    }

    return this.batchClient;
  }

  public AmazonS3 getS3Client() {

    if(this.s3Client == null) {
      this.s3Client = AmazonS3Client.builder().build();
    }

    return this.s3Client;
  }

  /**
   * Gets the default VPC network configured in the default AWS region.
   * This is used to extract the configured subnets for the default region.
   *
   * @return vpc instance pointing to the default configured VPC
   */
  public Vpc getDefaultVpc() {

    if(this.defaultVpc == null) {

      //Build a filter for the default VPC
      var defaultVpcFilter = List.of(new Filter("isDefault", List.of("true")));

      var describeVpcRequest = new DescribeVpcsRequest();
      describeVpcRequest.setFilters(defaultVpcFilter);

      var defaultVpcResponse = this.getAmazonClient().describeVpcs(describeVpcRequest);

      if (!defaultVpcResponse.getVpcs().isEmpty()) {
        this.defaultVpc = defaultVpcResponse.getVpcs().get(0);
      } else {
        throw new RuntimeException("No default VPC found. Cannot proceed");
      }
    }

    return this.defaultVpc;
  }

  /**
   * Returns the provided environment configuration JSON as string.
   * @return
   */
  public String getEnvironmentConfiguration() {

    String result = null;
    try {
      result = Files.readString(Paths.get(this.configuredInfrastructure.getEnvironment()));
    } catch (IOException e) {
      throw new RuntimeException(
              String.format("Could not read environment configuration file from %s", this.configuredInfrastructure.getEnvironment()));
    }

    return result;
  }

  public void setComputEnvironmentName(String computeEnvironmentName){
    this.computeEnvironmentName = computeEnvironmentName;
  }

  public String getComputeEnvironmentName() {
    return this.computeEnvironmentName;
  }

  /**
   * Gets a configured parameter from the experiments section in the DSL
   * @param parameterName
   * @return parameter object
   */
  public Parameter getParameter(String parameterName) {
    var parameter = this.configuredInfrastructure.getParameters()
            .stream().filter(p -> p.getName().equals(parameterName)).findFirst();

    if(!parameter.isPresent()) {
      throw new RuntimeException(
              String.format("Could not find %s parameter for infrastructure %s", parameterName, configuredInfrastructure.getName()));
    }

    return parameter.get();
  }
}