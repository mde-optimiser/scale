package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws;

import com.amazonaws.services.batch.model.SubmitJobResult;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.IProvider;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Infrastructure;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSInfrastructureProvider implements IProvider {

  private final BatchParametersProvider batchParametersProvider;
  private final ComputeEnvironmentRequestWrapper computeEnvironmentRequestWrapper;
  private final JobDefinitionWrapper jobDefinitionWrapper;
  private final QueueRequestWrapper queueRequestWrapper;
  private final StorageRequestWrapper storageServiceWrapper;
  private Infrastructure configuredInfrastructure;
  private final HashMap<IScaleTask, String> jobDefinitionSpecifications;
  private final JobWrapper jobWrapper;

  public AWSInfrastructureProvider(Infrastructure configuredInfrastructure) {

    this.configuredInfrastructure = configuredInfrastructure;
    this.batchParametersProvider = new BatchParametersProvider(configuredInfrastructure);
    this.computeEnvironmentRequestWrapper = new ComputeEnvironmentRequestWrapper(this.batchParametersProvider);
    this.jobDefinitionWrapper = new JobDefinitionWrapper(this.batchParametersProvider);
    this.queueRequestWrapper = new QueueRequestWrapper(this.batchParametersProvider);
    this.storageServiceWrapper = new StorageRequestWrapper(this.batchParametersProvider);
    this.jobDefinitionSpecifications = new HashMap<IScaleTask, String>();
    this.jobWrapper = new JobWrapper(this.batchParametersProvider);
  }

  @Override
  public void build() {

    if(!isValidAccount()){
      throw new RuntimeException("Invalid infrastructure account configuration. Execution stopped");
    }

    if(!hasRequiredPermissions()) {
      throw new RuntimeException("Invalid infrastructure account permissions. Execution stopped.");
    }

    //Check if the default S3 bucket exists if not create it
    createStorage();

    //Check if the configured environment exists, and is active. Not not create or activate it
    createEnvironment();

    //Create Job queue
    createJobQueue();

  }

  @Override
  public boolean isValidAccount() {

    //Should check credentials
    //Should check default region

    //TODO For now we assume the best possible case
    return true;
  }

  @Override
  public boolean hasRequiredPermissions() {

    //should check roles
    //TODO Assume everything is configured correctly by the user
    return true;
  }

  @Override
  public boolean createStorage() {

    this.storageServiceWrapper.createDefaultBucket();

    //Create default tool bucket
    return false;
  }

  @Override
  public String uploadFiles(ExperimentConfiguration experimentConfiguration) {

    //Upload the global experiment artifacts
    this.storageServiceWrapper.uploadFiles(experimentConfiguration.getEperimentArtifacts());

    //Upload the specific task artifacts
    this.storageServiceWrapper.uploadFiles(experimentConfiguration.getAllTasksArtifacts());

    return this.storageServiceWrapper.getDefaultBucketName();
  }

  @Override
  public boolean uploadFiles(Map<String, File> files) {
    return this.storageServiceWrapper.uploadFiles(files);
  }

  @Override
  public boolean createEnvironment() {

    //Check environment exists
    var computeEnvironment = this.computeEnvironmentRequestWrapper.describeComputeEnvironmentRequest();

    //If yes, is active?
    if(!computeEnvironment.getComputeEnvironments().isEmpty()){

      //If it's not active
      if(!computeEnvironment.getComputeEnvironments().get(0).getState().equals("ENABLED")){
          //Activate it
          this.computeEnvironmentRequestWrapper.activateComputEnvironment();
      }
    } else {
      //Create a new environment
      var response = this.computeEnvironmentRequestWrapper.createComputeEnvironment();
    }

    //If no exception is thrown, then we're fine
    //TODO: Add some better error handling around this wrapper
    return true;
  }

  @Override
  public boolean createJobQueue() {

    var jobQueues = this.queueRequestWrapper.describeJobQueues().getJobQueues();

    //if job queue exists and not connected
    if(!jobQueues.isEmpty()) {

      //is it connected to our environment?
      if(!jobQueues.get(0)
              .getComputeEnvironmentOrder()
              .get(0).getComputeEnvironment().equals(this.batchParametersProvider.getComputeEnvironmentName())){

        var response = this.queueRequestWrapper.connectJobQueueToEnvironment();
      }
    } else {

      // If the job queue does not exist already, create it and link it to our existing comput environment
      var response = this.queueRequestWrapper.createJobQueue();
    }
    return false;
  }

  @Override
  public boolean createJobDefinitions(String experimentName, List<IScaleTask> tasks) {

    //For each task, get the unique task type name
    tasks.forEach(task -> {

      var result = this.jobDefinitionWrapper.registerJobDefinition(experimentName, task);

      //Log the job definition name for this task
      this.jobDefinitionSpecifications.put(task, result.getJobDefinitionName());
    });

    return true;
  }

  @Override
  public SubmitJobResult queueJob(ScaleJob job) {

    var jobDefinition = this.getJobDefinition(job);
    var jobQueueName = this.queueRequestWrapper.getJobQueueName();
    var jobResult = this.jobWrapper.submitJobRequest(job, jobDefinition, jobQueueName);

    return jobResult;
  }

  /**
   * Get a registered job definition for the current job.
   * @param job
   * @return
   */
  private String getJobDefinition(ScaleJob job){

    if (this.jobDefinitionSpecifications.containsKey(job.getTask())){
      return this.jobDefinitionSpecifications.get(job.getTask());
    }

    throw new RuntimeException(String.format("Could not find job definition for task: %s", job.getName()));
  }

}