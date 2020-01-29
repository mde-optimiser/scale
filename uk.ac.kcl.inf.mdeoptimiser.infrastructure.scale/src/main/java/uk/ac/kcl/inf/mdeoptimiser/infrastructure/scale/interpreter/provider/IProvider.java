package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider;

import com.amazonaws.services.batch.model.SubmitJobResult;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * TODO This doesn't exactly match extensibility across multiple clouds
 */
public interface IProvider {

  /**
   * Checks if the provided account credentials are valid for this provider.
   * This will mean that the name of the configured aws environment is valid
   * @return true if the configured account is valid.
   */
  boolean isValidAccount();

  /**
   * Checks if the provided account credentials have the required permissions for this
   * tool to run as configured.
   * @return true if the permissions are valid
   */
  boolean hasRequiredPermissions();

  /**
   * Creates the storage layer
   * @return true if the storage layer has been created
   */
  boolean createStorage();

  /**
   * Upload experiment files to the common storage layer used by this cloud provider.
   * @param experimentConfiguration
   * @return name of the bucket where the artifacts are uploaded
   */
  String uploadFiles(ExperimentConfiguration experimentConfiguration);

  /**
   * Upload an additional given set of files
   * @return true if the files have been uploaded. false otherwise
   */
  boolean uploadFiles(Map<String, File> files);

  /**
   * Create the computatonal layer using the user provided configuration
   * @return
   */
  boolean createEnvironment();

  boolean createJobQueue();

  /**
   * Registers job definition specifications for the specified jobs.
   * @param experimentName
   * @param tasks
   * @return a map of the configured task and the corresponding job definition name
   */
  boolean createJobDefinitions(String experimentName, List<IScaleTask> tasks);

  //Queues a job for execution
  SubmitJobResult queueJob(ScaleJob jobs);

  void build();
}
