package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws;

import com.amazonaws.services.batch.model.*;
import com.google.gson.Gson;

public class ComputeEnvironmentRequestWrapper {

  private BatchParametersProvider batchParameterProvider;
  private String computeEnvironmentName;
  private CreateComputeEnvironmentRequest createComputeEnvironmentRequest;

  public ComputeEnvironmentRequestWrapper(BatchParametersProvider batchParameterProvider){

    this.batchParameterProvider = batchParameterProvider;
    this.parseConfiguredComputeEnvironment();
    this.batchParameterProvider.setComputEnvironmentName(this.computeEnvironmentName);
  }


  private void parseConfiguredComputeEnvironment() {

    if(this.createComputeEnvironmentRequest == null) {
      var environmentConfiguration = batchParameterProvider.getEnvironmentConfiguration();
      this.createComputeEnvironmentRequest = new Gson().fromJson(environmentConfiguration, CreateComputeEnvironmentRequest.class);
      this.computeEnvironmentName = this.createComputeEnvironmentRequest.getComputeEnvironmentName();
    }
  }

  //Read
  public DescribeComputeEnvironmentsRequest createDescribeComputeEnvironmentRequest(){
    return new DescribeComputeEnvironmentsRequest().withComputeEnvironments(this.computeEnvironmentName);
  }

  public DescribeComputeEnvironmentsResult describeComputeEnvironmentRequest(){
    var request = this.createDescribeComputeEnvironmentRequest();
    return this.batchParameterProvider.getBatchClient().describeComputeEnvironments(request);
  }


  // Create
  public CreateComputeEnvironmentRequest createComputeEnvironmentRequest(){
    return this.createComputeEnvironmentRequest;
  }

  public CreateComputeEnvironmentResult createComputeEnvironment(){

    var request = this.createComputeEnvironmentRequest();

    return this.batchParameterProvider.getBatchClient().createComputeEnvironment(request);
  }


  //Delete

  public DeleteComputeEnvironmentRequest deleteComputeEnvironmentRequest() {

    return new DeleteComputeEnvironmentRequest().withComputeEnvironment( this.computeEnvironmentName);
  }

  public DeleteComputeEnvironmentResult deleteComputeEnvironment() {

    var request = this.deleteComputeEnvironmentRequest();
    return this.batchParameterProvider.getBatchClient().deleteComputeEnvironment(request);
  }

  // Activate an existing inactive compute environment
  public void activateComputEnvironment() {
    var request = new UpdateComputeEnvironmentRequest().withComputeEnvironment(this.computeEnvironmentName).withState("ENABLED");
    this.batchParameterProvider.getBatchClient().updateComputeEnvironment(request);
  }
}