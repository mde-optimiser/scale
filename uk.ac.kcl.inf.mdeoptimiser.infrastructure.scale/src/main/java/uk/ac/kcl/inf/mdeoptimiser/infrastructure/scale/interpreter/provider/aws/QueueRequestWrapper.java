package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws;


import com.amazonaws.services.batch.model.*;

public class QueueRequestWrapper {


  private BatchParametersProvider batchParametersProvider;

  public QueueRequestWrapper(BatchParametersProvider batchParametersProvider){

    this.batchParametersProvider = batchParametersProvider;
  }

  //Read
  public DescribeJobQueuesResult describeJobQueues() {

    var request = new DescribeJobQueuesRequest().withJobQueues(this.getJobQueueName());
    return this.batchParametersProvider.getBatchClient().describeJobQueues(request);
  }

  //Create job queue

  public String getJobQueueName(){
    return String.format("MDEOScale_%s", this.batchParametersProvider.getComputeEnvironmentName());
  }

  public CreateJobQueueRequest createJobQueueRequest(){

    var computeEnvironmentName = this.batchParametersProvider.getComputeEnvironmentName();

    var jobQueueRequest = new CreateJobQueueRequest()
            .withJobQueueName(this.getJobQueueName())
            .withState("ENABLED")
            .withPriority(1)
            .withComputeEnvironmentOrder(
                    new ComputeEnvironmentOrder()
                            .withOrder(1)
                            .withComputeEnvironment(computeEnvironmentName));


    return jobQueueRequest;
  }

  public CreateJobQueueResult createJobQueue(){

    var request = this.createJobQueueRequest();

    return this.batchParametersProvider.getBatchClient().createJobQueue(request);
  }


  public DeleteJobQueueRequest deleteJobQueueRequest(){

    var deleteJobQueueRequest = new DeleteJobQueueRequest().withJobQueue(this.getJobQueueName());
    return deleteJobQueueRequest;
  }

  public DeleteJobQueueResult deleteJobQueue(){

    var request = this.deleteJobQueueRequest();
    return this.batchParametersProvider.getBatchClient().deleteJobQueue(request);

  }

  public UpdateJobQueueResult connectJobQueueToEnvironment() {

    //Set only our compute environment to be linked to this queue
    var computEnvironmentOrder = new ComputeEnvironmentOrder();
    computEnvironmentOrder.setComputeEnvironment(this.batchParametersProvider.getComputeEnvironmentName());

    var request = new UpdateJobQueueRequest()
            .withJobQueue(this.getJobQueueName()).withComputeEnvironmentOrder(computEnvironmentOrder);
    return this.batchParametersProvider.getBatchClient().updateJobQueue(request);

  }
}
