package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types;

import com.amazonaws.services.batch.model.ContainerProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Dependency;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Task;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractScaleTask implements IScaleTask {

  protected String name;
  protected String modelName;
  protected String experimentInstanceName;
  protected String experimentName;
  protected Map<String, File> taskFiles;
  protected Map<String, File> experimentArtifacts;
  protected String id;
  protected String command;
  private HashMap<String, File> taskDependencies;

  public AbstractScaleTask(ExperimentConfiguration experimentConfiguration, Model model, Task task){

    this.experimentInstanceName = experimentConfiguration.getExperimentInstanceName();
    this.experimentName = experimentConfiguration.getExperimentName();
    this.setExperimentArtifacts(experimentConfiguration.getEperimentArtifacts());
    this.name = task.getName();
    this.modelName = model.getName();
    this.command = task.getRun().getCommand();
    this.setDependencies(task.getDependencies());
    this.setId();
    validateTask();
  }

  public AbstractScaleTask(){}

  @Override
  public String getName() {

    return this.name;
  }

  private void setExperimentArtifacts(Map<String, File> experimentArtifacts){
    this.experimentArtifacts = experimentArtifacts;
  }

  @Override
  public Map<String, File> getExperimentArtifacts() {
    return this.experimentArtifacts;
  }

  @Override
  public Map<String, File> getTaskFiles() {

    if(this.taskFiles == null) {

      //The command file is part of the task files
      var command = this.getCommand();

      //TODO This might require some cleansing here
      this.taskFiles = new HashMap<String, File>();
      taskFiles.put(getKeyPrefix(command), new File(command));

      //The dependencies are part of the task files
      //Might need more specific prefix here
      taskFiles.putAll(this.getDependencies());
    }

    return this.taskFiles;
  }

  private String getKeyPrefix(String key){
    return Paths.get(this.getExperimentInstanceNamePrefix(),
            this.getExperimentModelNamePrefix(), key).toString();
  }

  @Override
  public String getExperimentInstanceNamePrefix(){
    return this.experimentInstanceName;
  }

  @Override
  public String getExperimentModelNamePrefix(){
    return Paths.get(this.modelName, this.getName()).toString();
  }

  public void setDependencies(Dependency dependency){
    //TODO assume one dependency for now in a single JAR
    //Might want to specify several dependencies in the DSL, maybe pom even?
    this.taskDependencies = new HashMap<String, File>();
    this.taskDependencies.put(getKeyPrefix(dependency.getName()), new File(dependency.getName()));
  }

  @Override
  public Map<String, File> getDependencies() {
    return taskDependencies;
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
    containerProperties.setProperty("memory", "2500");
    containerProperties.setProperty("command", "java -jar /var/app/current/scale-wrapper.jar");

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
  public String getExperimentName(){
    return this.experimentName;
  }

  @Override
  public String getModelName(){
    return this.modelName;
  }

  @Override
  public String getBatchResultsKey(int batchNumber) {
    return String.format("%s/results/%s/batch-%s/accumulator.csv", this.getExperimentInstanceNamePrefix(),
            this.getExperimentModelNamePrefix(), "" + batchNumber);

  }
}