package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types;

import com.amazonaws.services.s3.AmazonS3Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IResultsProcessingTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.ScaleTaskAdapter;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.TaskType;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws.JobWrapper;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws.StorageRequestWrapper;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJob;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJobResult;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.ResultsAnalyser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Task to generate result processing jobs. This will call the experiments interpreter and schedule results
 * processing jobs that are dependent on the
 */
public class ResultsTask implements IScaleTask, IResultsProcessingTask {

  private final String className = this.getClass().getName();

  private transient List<ScaleJob> scheduledJobs;
  private String jobsLog;

  protected String experimentInstanceName;
  protected String id;
  private transient JobWrapper jobWrapper;
  private transient ResultsAnalyser resultsAnalyser;

  public ResultsTask(ExperimentConfiguration experimentConfiguration, List<ScaleJob> scheduledJobs) {

    this.experimentInstanceName = experimentConfiguration.getExperimentInstanceName();
    this.setScheduledJobs(scheduledJobs);
    this.jobWrapper = new JobWrapper();
  }

  public ResultsTask(){
    this.jobWrapper = new JobWrapper();
    this.resultsAnalyser = new ResultsAnalyser();
  }

  public void setScheduledJobs(List<ScaleJob> scheduledJobs){
    this.scheduledJobs = scheduledJobs;
  }

  public JobWrapper getJobWrapper() {
    return this.jobWrapper;
  }

  @Override
  public String getClassName() {
    return this.className;
  }

  @Override
  public TaskType getType() {
    return TaskType.RESULTS;
  }

  @Override
  public String getName() {
    return "ResultsProcessing";
  }

  @Override
  public String getModelName() {
    return "All";
  }

  @Override
  public Map<String, File> getDependencies() {
    return null;
  }

  @Override
  public String getCommand() {
    return null;
  }

  @Override
  public boolean validateTask() {
    return false;
  }

  @Override
  public Map<String, File> getExperimentArtifacts() {
    return null;
  }

  @Override
  public Map<String, File> getTaskFiles() {

    return Map.of(this.jobsLog, Paths.get(this.jobsLog).toFile());

  }

  /**
   * By default MDEO and MoMOT run in the same container
   * @return object containing the container properties
   */
  @Override
  public Properties getContainerProperties() {

    var containerProperties = new Properties();

    containerProperties.setProperty("image", "goustaveb/results");
    containerProperties.setProperty("vcpus", "1");
    containerProperties.setProperty("memory", "2048");
    containerProperties.setProperty("command", "java -jar /var/app/scale-wrapper.jar");

    return containerProperties;
  }

  private void setId(){
    this.id = String.format("%s_%s", this.getType().toString(), java.util.UUID.randomUUID());
  }

  @Override
  public String getId(){

    if(this.id == null){
      this.setId();
    }

    return this.id;
  }

  @Override
  public String getExperimentName() {
    return "ResultsProcessing";
  }

  @Override
  public String getExperimentInstanceNamePrefix() {
    return this.experimentInstanceName;
  }

  @Override
  public String getExperimentModelNamePrefix() {
    return null;
  }

  @Override
  public String getToolName() {
    return null;
  }

  @Override
  public ScaleJobResult run(int batchNumber) {

    try {
      //Download the list of jobs
      //Sync files from the configured aws paths
      System.out.println("Get job log from S3");

      //TODO move this to DI
      var storageRequestWrapper = new StorageRequestWrapper(AmazonS3Client.builder().build());

      //Unserialise jobs list
      //Download the task specific files
      storageRequestWrapper.downloadFiles(this.getTaskFiles());

      //Wait for the other jobs to finish
      while(!this.areResultsReady()) {
        //Wait for 5 minutes before checking again
        Thread.sleep(300000);
      }

      System.out.println("All jobs succeeded. Starting to download results");

      storageRequestWrapper.downloadFiles(this.getResultsFilePaths());

      resultsAnalyser.runResultsAnalysis(this.getExperimentInstanceNamePrefix(), this.getScheduledJobs());

    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    }

    return null;
  }

  @Override
  public List<Path> getResultLocations() {
    return null;
  }

  @Override
  public Path getResultsAccumulator() {
    return null;
  }

  @Override
  public String getBatchResultsKey(int batchId) {
    return null;
  }

  /**
   * Pool the job queue to see if the logged jobs have completed successfully or not.
   * @return true when all the jobs are completed.
   */
  private boolean areResultsReady() {

    var scheduledJobs =  this.getScheduledJobs();

    var jobIDs = scheduledJobs.stream().map(job -> job.getSubmitJobResult().getJobId()).collect(Collectors.toList());

    var jobResult = this.getJobWrapper().describeJobs(jobIDs);

    var allJobsSucceeded = true;

    for(var job : jobResult.getJobs()){
      if(!job.getStatus().equals("SUCCEEDED")){
        allJobsSucceeded = false;
      }
    }

    return allJobsSucceeded;
  }

  @Override
  public String getJobsLogFileKey() {

    if(this.jobsLog == null){
      this.jobsLog = String.format("%s/results/mdeo-scale-jobs.json", this.getExperimentInstanceNamePrefix());
    }

    return this.jobsLog;
  }

  @Override
  public Map<String, File> getScheduledJobsLog() {

    Map<String, File> scheduledJobsLog = Collections.emptyMap();

    try {
      File temporaryJobsLogFile = Files.createTempFile("mdeo-scale-jobs-", ".json").toFile();
      temporaryJobsLogFile.deleteOnExit();

      FileWriter fileWriter = new FileWriter(temporaryJobsLogFile, Charset.defaultCharset());

      //Serialise the jobs and write them to a temporary file
      Gson gson = new Gson();
      fileWriter.write(gson.toJson(this.scheduledJobs));
      fileWriter.flush();

      scheduledJobsLog = Map.of(this.getJobsLogFileKey(), temporaryJobsLogFile);

    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not create temporary jobs log file");
    }

    return scheduledJobsLog;
  }

  @Override
  public Map<String, File> getResultsFilePaths() {
    var resultsPath = String.format("%s/results/", this.getExperimentInstanceNamePrefix());
    return Map.of(resultsPath, Paths.get(resultsPath).toFile());
  }

  /**
   * Get the scheduled jobs.
   * @return
   */
  public List<ScaleJob> getScheduledJobs(){

    if(this.scheduledJobs == null) {
      this.scheduledJobs = this.loadScheduledJobsLog(Paths.get(this.jobsLog).toFile());
    }

    return this.scheduledJobs;
  }

  /**
   * Load the queued jobs from the jobs log file.
   * @return list of queued scale jobs
   */
  public List<ScaleJob> loadScheduledJobsLog(File jobLog) {

    try {

      Type listType = new TypeToken<ArrayList<ScaleJob>>(){}.getType();
      var jobLogReader = new FileReader(jobLog, Charset.defaultCharset());
      //Serialise the jobs and write them to a temporary file
      final GsonBuilder builder = new GsonBuilder();

      builder.registerTypeAdapter(IScaleTask.class, new ScaleTaskAdapter<IScaleTask>());
      final Gson gson = builder.create();

      List<ScaleJob> scaleJobs = gson.fromJson(jobLogReader,listType);

      return scaleJobs;

    } catch (Exception e){
      throw new RuntimeException("Unable to parse scheduled jobs log from JSON." + e.getMessage());
    }
  }
}
