package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws;

import com.amazonaws.services.batch.model.*;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;

import java.util.Properties;

public class JobDefinitionWrapper {

  private BatchParametersProvider batchParametersProvider;

  public JobDefinitionWrapper(BatchParametersProvider batchParametersProvider){

    this.batchParametersProvider = batchParametersProvider;
  }

  /**
   * Load AWS container properties from a scale task specification
   * @param containerProperties
   * @return container properties instance
   */
  public ContainerProperties getContainerProperties(Properties containerProperties) {

    var container = new ContainerProperties()
            .withImage(containerProperties.getProperty("image"))
            .withJobRoleArn(this.getJobRoleParameter())
            .withVcpus(Integer.parseInt(containerProperties.getProperty("vcpus")))
            .withMemory(Integer.parseInt(containerProperties.getProperty("memory")));

    return container;
  }

  /**
   * Get the configured job role parameter
   *
   * TODO move this code and the batch parameter parsing to a parameters factory. Add validation in the DSL if one
   * required param is missing.
   *
   * @return string containing the configured AWS job role parameter
   */
  public String getJobRoleParameter() {

    var parameterValue = this.batchParametersProvider.getParameter("job.role");
    return parameterValue.getValue().getPath();

  }

  /**
   * Get the job definition name, whic is unique per task. For the same experiment tasks can have dfferent job definitions
   * to allow for further customisation of infrastructure, hardware specs, on a per model basis.
   * @param task instance of the current job definition
   * @return
   */
  public String getJobDefinitionName(String experimentName, IScaleTask task){
    //TODO Job definition name length must be < 128 chars
    var jobDefinitionName = String.format("exp-%s-type-%s-name-%s-model-%s", experimentName, task.getType(), task.getName(), task.getModelName());

    //Remove all but these allowed characters from the job name
    return jobDefinitionName.replaceAll("[^a-zA-Z0-9-_]","");
  }

  /**
   * Creates a job definition registration request, used to create a job definition request with the configured
   * type and the configured container properties.
   * @return instance of the job definition request
   */
  private RegisterJobDefinitionRequest registerJobDefinitionRequest(String experimentName, IScaleTask task){

    var jobDefinitionName = this.getJobDefinitionName(experimentName, task);

    var containerProperties = this.getContainerProperties(task.getContainerProperties());

    return new RegisterJobDefinitionRequest()
            .withJobDefinitionName(jobDefinitionName)
            .withType("container") //This is the only supported type
            .withContainerProperties(containerProperties);
  }

  /**
   * Makes a job definition registration request
   * @return instance of the job definition registration request result
   */
  public RegisterJobDefinitionResult registerJobDefinition(String experimentName, IScaleTask task){

    var registerJobDefinitionRequest = this.registerJobDefinitionRequest(experimentName, task);

    return this.batchParametersProvider.getBatchClient().registerJobDefinition(registerJobDefinitionRequest);
  }

  /**
   * Creates a job definition deregistration request, used to deregister a job definition by the given name.
   * @return instance of the deregistration job definition request
   */
  public DeregisterJobDefinitionRequest deregisterJobDefinitionRequest(String jobDefinitionName){

    return  new DeregisterJobDefinitionRequest()
            .withJobDefinition(jobDefinitionName);
  }

  /**
   * Deregisters a job definition
   * @return deregisterJobDefinitionResult containing the deregistration request status
   */
  public DeregisterJobDefinitionResult deregisterJobDefinition(String jobDefinitionName){

    var deregisterJobDefinitionRequest = this.deregisterJobDefinitionRequest(jobDefinitionName);

    return this.batchParametersProvider.getBatchClient().deregisterJobDefinition(deregisterJobDefinitionRequest);
  }

}
