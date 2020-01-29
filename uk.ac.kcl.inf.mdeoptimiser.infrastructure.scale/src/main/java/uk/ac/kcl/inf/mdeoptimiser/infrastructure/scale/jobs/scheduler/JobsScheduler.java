package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.scheduler;

import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.IProvider;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;

import java.util.List;

public class JobsScheduler {

  private IProvider infrastructureProvider;

  public JobsScheduler(IProvider infrastructureProvider){
    this.infrastructureProvider = infrastructureProvider;
  }

  /**
   * Schedule the jobs for execution and store the scheduled job details for each scheduled
   * job. The job name and id are used to check if all the jobs have been executed successfully or not.
   * @param jobs
   */
  public void scheduleExperimentJobs(List<ScaleJob> jobs) {

    jobs.forEach(job -> {
      var submitJobResult = this.infrastructureProvider.queueJob(job);

      //Store the result request for this job
      job.setSubmitJobResult(submitJobResult);
    });
  }

}
