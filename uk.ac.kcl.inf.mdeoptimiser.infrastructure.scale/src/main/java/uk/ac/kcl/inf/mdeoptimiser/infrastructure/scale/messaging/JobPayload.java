package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.ScaleTaskAdapter;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POJO Wrapper for the scale job setting the current serialized job as a parameter for a batch task.
 * This generates the -payload parameter with the value set to a serialised instance of the current job.
 */
public class JobPayload {

  private ScaleJob job;

  public JobPayload(ScaleJob job){
    this.setJob(job);
  }

  public void setJob(ScaleJob job){
    this.job = job;
  }

  public ScaleJob getJob(){
    return this.job;
  }

  /**
   * Prepare and serialise the job payload to JSON. This gets passed to the tool service wrapper.
   * @return
   */
  public static List<String> getJobParameters(ScaleJob job) {
    return List.of("-payload", new Gson().toJson(new JobPayload(job)));
  }

  /**
   * Deserialize the job object with the custom IScaleTask implementation adapter.
   * @param json string containing the payload
   * @return jobPayload instance passed to this job
   */
  public static JobPayload deSerialiseJob(String json) {
    final GsonBuilder builder = new GsonBuilder();

    builder.registerTypeAdapter(IScaleTask.class, new ScaleTaskAdapter<IScaleTask>());
    final Gson gson = builder.create();

    return gson.fromJson(json, JobPayload.class);
  }
}