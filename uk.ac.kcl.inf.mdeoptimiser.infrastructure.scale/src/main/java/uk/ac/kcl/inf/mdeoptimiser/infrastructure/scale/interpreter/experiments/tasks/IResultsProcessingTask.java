package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks;

import java.io.File;
import java.util.Map;

public interface IResultsProcessingTask {

  /**
   * Returns a reference to the file containing the scheduled job ids. This is used by the results processing job
   * to check that all jobs are completed before the execution is started.
   * @return file containing a list of scheduled jobs.
   */
  Map<String, File> getScheduledJobsLog();

  Map<String, File> getResultsFilePaths();

  String getJobsLogFileKey();

}
