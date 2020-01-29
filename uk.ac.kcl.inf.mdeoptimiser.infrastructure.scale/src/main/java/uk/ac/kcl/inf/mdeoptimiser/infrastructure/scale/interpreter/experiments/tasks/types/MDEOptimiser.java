package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types;

import com.amazonaws.services.s3.AmazonS3Client;
import org.zeroturnaround.exec.ProcessExecutor;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.TaskType;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws.StorageRequestWrapper;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.ScaleJobResult;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class MDEOptimiser extends AbstractScaleTask {

  private static final String MDEO_MAIN_CLASS = "uk.ac.kcl.inf.mdeoptimiser.interfaces.cli.Run";
  private static final String MDEO_TOOL_NAME = "MDEOptimiser";
  private final String className = this.getClass().getName();

  public MDEOptimiser(){
  }

  public MDEOptimiser(ExperimentConfiguration experimentConfiguration, Model model, Task taskSpec) {
    super(experimentConfiguration, model, taskSpec);
  }

  @Override
  public String getClassName() {
    return this.className;
  }

  @Override
  public TaskType getType() {
    return TaskType.MDEO;
  }

  @Override
  public String getCommand() {
    return this.command;
  }

  @Override
  public boolean validateTask() {

    if(!this.command.endsWith(".mopt")){
      throw new RuntimeException("Unexpected command extension. Expecting a mopt file for this task type");
    }

    return false;
  }

  @Override
  public String getToolName() {
    return MDEO_TOOL_NAME;
  }

  @Override
  public ScaleJobResult run(int batchNumber) {

    //Sync files from the configured aws paths
    System.out.println("Get task artifacts artifacts from S3");

    //TODO move this to DI
    var storageRequestWrapper = new StorageRequestWrapper(AmazonS3Client.builder().build());

    //Download the task specific files
    storageRequestWrapper.downloadFiles(this.getTaskFiles());

    //Download the globally configured experiment files
    storageRequestWrapper.downloadFiles(this.getExperimentArtifacts());

    //Run the configuration
    try {

      var outputStream = new FileOutputStream(this.consoleOutputLocation());

      System.out.println("Starting execution");

      var executor = new ProcessExecutor()
              .command("java", "-cp", this.getClasspath(),
                      MDEO_MAIN_CLASS, "-p", "./", "-m", this.getCommand(), "-b", "" + batchNumber)
              .redirectOutput(outputStream)
              .redirectOutput(System.out)
              .execute();

      if(executor.getExitValue() == 0){
        System.out.println("Execution terminated successfully");
      } else {
        throw new RuntimeException(String.format("Unable to start MDEOptimiser search. Return code %s", executor.getExitValue()));
      }

    } catch(Exception e) {
      throw new RuntimeException(
              String.format("Unable to run standalone MDEOptimiser process. Encountered exception: %s", e.getStackTrace()));
    }



    //Upload the generated results files
    System.out.println("Publish generated artifacts.");

    var resultsAccumulator = this.getResultsAccumulator();

    System.out.println(String.format("Upload results accumulator to %s", resultsAccumulator));

    storageRequestWrapper.uploadFiles(Map.of(this.getBatchResultsKey(batchNumber), resultsAccumulator.toFile()));


    storageRequestWrapper.shutdown();

    //TODO Return an instance of the ScaleJobResult object
    return null;
  }

  /**
   * All MDEOptimiser results are always stored in a folder called mdeo-results.
   * This folder will be uploaded to the configured upload location for this task.
   * @return list containing the paths inside the container where results artifacts are generated by the current task.
   */
  @Override
  public List<Path> getResultLocations() {
    return List.of(Paths.get("./mdeo-results/"));
  }

  @Override
  public Path getResultsAccumulator() {

    var resultsPath = this.getResultLocations();

    try {
      var file = Files.find(resultsPath.get(0),
              Integer.MAX_VALUE,
              (path, basicFileAttributes) -> path.toFile().getName().matches(".*.csv")
              ).findFirst().get();

      return file;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public File consoleOutputLocation(){

    var outputFile = Path.of("./mdeo-results/console-output.log").toFile();

    if(!outputFile.getParentFile().exists()){
      outputFile.getParentFile().mkdirs();
    }
    return outputFile;
  }

  /**
   * By default MDEO and MoMOT run in the same container
   * @return object containing the container properties
   */
  @Override
  public Properties getContainerProperties() {

    var containerProperties = new Properties();

    containerProperties.setProperty("image", "goustaveb/mdeoptimiser");
    containerProperties.setProperty("vcpus", "1");
    containerProperties.setProperty("memory", "2048");
    containerProperties.setProperty("command", "java -jar /var/app/current/scale-wrapper.jar");

    return containerProperties;
  }

  /**
   * Load the classpath for running MDEOptimiser inside the docker container.
   *
   * The /var/app/dependencies is hardcoded here and this assumes that the files will be placed in this respective
   * path in the docker container used to run these experiments.
   * @return string containing the java classpath for running MDEOptimiser along with all the configured user
   * dependencies.
   */
  private String getClasspath(){

    var classpath = new StringBuilder();

    //Add the default classpath that contains the tool jars
    classpath.append(".:/var/app/dependencies/*");

    //Add the user configured dependencies
    classpath.append(this.getDependencies()
            .values().stream().map(File::toString).collect(Collectors.joining(":", ":", "")));

    return classpath.toString();
  }

}