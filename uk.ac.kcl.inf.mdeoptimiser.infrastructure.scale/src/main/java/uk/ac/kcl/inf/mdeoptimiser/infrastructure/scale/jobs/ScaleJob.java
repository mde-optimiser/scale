package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs;

import com.amazonaws.services.batch.model.SubmitJobResult;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;

public class ScaleJob {

  private IScaleTask task;
  private String jobName;

  /**
   * The number of the current batch for this job
   */
  private int batchNumber;

  /**
   * Result of the submit job request. This contains a reference to the job id and name.
   * The value here will be used by the job pool worker to initialise the result parsing process
   * once all the jobs have been successfully executed.
   * @param submitJobResult
   */
  private SubmitJobResult submitJobResult;

  /**
   * Create a job for the task and batch number.
   * @param task
   * @param batchNumber
   */
  public ScaleJob(IScaleTask task, int batchNumber) {
    this.task = task;
    this.batchNumber = batchNumber;
  }

  /**
   * Constructor used to load results processing tasks. For these jobs the batch number defaults to
   * -1 bc there is no need for batches.
   * @param task
   */
  public ScaleJob(IScaleTask task){
    this(task, -1);
  }

  public String getName() {

    if(this.jobName == null){

      var jobName = String.format("exp_%s_model_%s_task_%s_batch_%s",
              this.getTask().getExperimentName(),
              this.getTask().getName(),
              this.getTask().getModelName(),
              batchNumber);

      //Remove all special chars from the job name
      this.jobName = jobName.replaceAll("[^a-zA-Z0-9-_]","");
    }

    return this.jobName;
  }

  /**
   * Getter for the task configured for this job instance.
   * @return instance of the task configured for this job.
   */
  public IScaleTask getTask() {
    return this.task;
  }

  /**
   * Execute the task configured for this job.
   * @return
   */
  public ScaleJobResult execute(){
    return this.task.run(this.getBatchNumber());
  }

  /**
   * Get the batch number configured for this job.
   * @return number of the current batch
   */
  public int getBatchNumber() {
    return this.batchNumber;
  }

  /**
   * Set the current batch number to use for this job
   * @param batchNumber
   */
  public void setBatchNumber(int batchNumber) {
    this.batchNumber = batchNumber;
  }

  /**
   * Save the result of the schedule job command.
   * @param submitJobResult
   */
  public void setSubmitJobResult(SubmitJobResult submitJobResult) {
    this.submitJobResult = submitJobResult;
  }

  public SubmitJobResult getSubmitJobResult() {
    return this.submitJobResult;
  }
}