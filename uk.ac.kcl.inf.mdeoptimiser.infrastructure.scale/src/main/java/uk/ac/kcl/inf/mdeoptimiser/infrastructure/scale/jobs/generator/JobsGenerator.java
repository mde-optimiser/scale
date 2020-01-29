package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.generator;

import com.amazonaws.services.batch.model.SubmitJobResult;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types.ResultsTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJobResult;

import java.util.LinkedList;
import java.util.List;

public class JobsGenerator {

  /**
   * Generates execution jobs from the configured tasks
   * @param experimentConfiguration
   * @return
   */
  public List<ScaleJob> getExperimentJobs(ExperimentConfiguration experimentConfiguration) {

    List<ScaleJob> jobs = new LinkedList<>();

    var tasks = experimentConfiguration.getTasks();
    var batches = experimentConfiguration.getExperimentBatches();

    tasks.forEach(task -> {

      // For pretty reporting start the batch counter from 1
      for(var batchNumber = 1; batchNumber <= batches; batchNumber++) {
        jobs.add(new ScaleJob(task, batchNumber));
      }
    });

    return jobs;
  }


  public ScaleJob getResultsAnalysisJobs(ExperimentConfiguration experimentConfiguration, List<ScaleJob> scheduledJobs){

    //Extract experiment
    var resultsProcessingTask = new ResultsTask(experimentConfiguration, scheduledJobs);
    var resultsProcessingJob = new ScaleJob(resultsProcessingTask);

    return resultsProcessingJob;
  }

}