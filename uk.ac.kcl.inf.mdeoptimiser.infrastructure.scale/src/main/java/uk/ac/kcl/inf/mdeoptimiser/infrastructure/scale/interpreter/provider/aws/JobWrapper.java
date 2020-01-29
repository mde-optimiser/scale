package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws;

import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.*;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.messaging.JobPayload;

import java.util.List;

public class JobWrapper {

  private AWSBatch batchClient;

  public JobWrapper(BatchParametersProvider batchParametersProvider){

    this.batchClient = batchParametersProvider.getBatchClient();
  }

  public JobWrapper() {
    this.batchClient = AWSBatchClientBuilder.standard().build();
  }

  //  AWSBatch client = AWSBatchClientBuilder.standard().build();
  //  SubmitJobRequest request =
  //  SubmitJobResult response = client.submitJob(request);

  public AWSBatch getBatchClient() {
    return this.batchClient;
  }

  //Submit job
  public SubmitJobRequest createSubmitJobRequest(ScaleJob job, String jobDefinition, String queueName){

    var containerOverrides = new ContainerOverrides();
    containerOverrides.setCommand(JobPayload.getJobParameters(job));

    var request = new SubmitJobRequest()
            .withJobName(job.getName())
            .withJobQueue(queueName)
            .withJobDefinition(jobDefinition)
            .withContainerOverrides(containerOverrides);

    return request;
  }

  public SubmitJobResult submitJobRequest(ScaleJob job, String jobDefinition, String queueName){
    var request = this.createSubmitJobRequest(job, jobDefinition, queueName);
    return this.getBatchClient().submitJob(request);
  }

  //Terminate job
  public TerminateJobRequest createTerminateJobRequest(String jobId){
    var request = new TerminateJobRequest().withJobId(jobId).withReason("UserTerminated");
    return request;
  }

  public TerminateJobResult terminateJob(String jobId){
    var request = this.createTerminateJobRequest(jobId);
    return this.getBatchClient().terminateJob(request);
  }

//  //Describe job
//  AWSBatch client = AWSBatchClientBuilder.standard().build();
//  DescribeJobsRequest request = new DescribeJobsRequest().withJobs("24fa2d7a-64c4-49d2-8b47-f8da4fbde8e9");
//  DescribeJobsResult response = client.describeJobs(request);
  public DescribeJobsRequest createDescribeJobRequest(String jobId){
    var request = new DescribeJobsRequest().withJobs(jobId);
    return request;
  }

  public DescribeJobsRequest createDescribeJobRequest(List<String> jobIds){
    var request = new DescribeJobsRequest().withJobs(jobIds);
    return request;
  }

  public DescribeJobsResult describeJob(String jobId){
    var request = this.createDescribeJobRequest(jobId);
    return this.getBatchClient().describeJobs(request);
  }

  public DescribeJobsResult describeJobs(List<String> jobIDs){

    var request = this.createDescribeJobRequest(jobIDs);
    return this.getBatchClient().describeJobs(request);

  }

  //List Jobs
//  AWSBatch client = AWSBatchClientBuilder.standard().build();
//  ListJobsRequest request = new ListJobsRequest().withJobQueue("HighPriority");
//  ListJobsResult response = client.listJobs(request);AWSBatch client = AWSBatchClientBuilder.standard().build();
//  ListJobsRequest request = new ListJobsRequest().withJobQueue("HighPriority").withJobStatus("SUBMITTED");
//  ListJobsResult response = client.listJobs(request);


}
