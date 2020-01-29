package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IScaleTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.TaskFactory;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Experiment;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Parameter;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Load the configured experiments and the corresponding tasks for each experiment.
 */
public class ExperimentConfiguration {

  private final Experiment experimentSpec;

  private final List<IScaleTask> tasks = new ArrayList<IScaleTask>();
  private File experimentArtifactsPath;
  private String experimentInstanceName;

  private Map<String, File> experimentArtifacts = new HashMap<String, File>();
  private Map<String, File> allTaskArtifacts = new HashMap<String, File>();

  //This is the master bucket in which all the files will be stored
  private final String BUCKET_NAME = "mdeo-scale";

  public ExperimentConfiguration(Experiment experiment){
    this.experimentSpec = experiment;
    loadTasks();
  }

  /**
   * Load the configured tasks for this experiment. If there are no tasks defined, throw an exception.
   * TODO Handle this case at the DSL validation layer. This case should never happen.
   */
  private void loadTasks(){


    this.experimentSpec.getModels().forEach(this::loadModelTasks);
  }

  private void loadModelTasks(Model model){

      System.out.println(String.format("Loading model %s", model.getName()));

      if (model.getTasks().isEmpty()) {
        throw new RuntimeException(String.format("Model \"%s\" does not have any recognised tasks.", model.getName()));
      }

      var taskFactory = new TaskFactory();

      model.getTasks().forEach(task -> {
        this.tasks.add(taskFactory.getTask(this, model, task));
        System.out.println(this.tasks.size());
      });


  }

  /**
   * Get the configured tasks for this experiment.
   * @return list containing the configured task instances.
   */
  public List<IScaleTask> getTasks(){

    if(this.tasks.isEmpty()){
      loadTasks();
    }

    return this.tasks;
  }

  //JobQueuePrefix
  public Map<String, File> getEperimentArtifacts() {

    if(this.experimentArtifacts.isEmpty()) {

      if (this.experimentArtifactsPath == null) {
        var artifactsParameter = this.getParameter("artifacts");

        //Currently we only support String values for this parameter
        this.experimentArtifactsPath = new File(artifactsParameter.getValue().getPath());

        if (!this.experimentArtifactsPath.exists()) {
          System.out.println(
                  String.format("Could not find experiment artifacts location: %s", this.experimentArtifactsPath.getPath()));
          System.exit(-1);
        }

        if (!this.experimentArtifactsPath.isDirectory()) {
          System.out.println(
                  String.format("Experiment artifacts path is not a directory: %s", this.experimentArtifactsPath.getAbsolutePath()));
          System.exit(-1);
        }
      }

      var pathMap = new HashMap<String, File>();

      pathMap.put(this.getKeyPrefix(this.experimentArtifactsPath.getPath()), this.experimentArtifactsPath);

      this.experimentArtifacts = pathMap;
    }

    return this.experimentArtifacts;
  }


  public Map<String, File> getAllTasksArtifacts() {

    if(this.allTaskArtifacts.isEmpty()) {
      var pathMap = new HashMap<String, File>();

      //TODO This assumes that there are no tasks with the same name for the same model in the same exp
      for(var task : this.getTasks()){
          pathMap.putAll(task.getTaskFiles());
      }

      this.allTaskArtifacts = pathMap;

    }

    return this.allTaskArtifacts;
  }

  /**
   * Get the configured number of batches that should be executed for each of the current experiment
   * tasks
   * @return number of batches to execute
   */
  public int getExperimentBatches(){

    var batchesParameter = this.getParameter("batches");
    return Integer.parseInt(batchesParameter.getValue().getNumeric());

  }

  private String getKeyPrefix(String key){
    return Paths.get(this.getExperimentInstanceName(), key).toString();
  }

  /**
   * Gets a configured parameter from the experiments section in the DSL
   * @param parameterName
   * @return parameter object
   */
  private Parameter getParameter(String parameterName) {
    var parameter = this.experimentSpec.getParameters()
            .stream().filter(p -> p.getName().equals(parameterName)).findFirst();

    if(!parameter.isPresent()) {
      throw new RuntimeException(
              String.format("Could not find %s parameter for experiment %s", parameterName, experimentSpec.getName()));
    }

    return parameter.get();
  }

  /**
   * Get the configured experiment name.
   * @return user defined name of the experiment
   */
  public String getExperimentName() {
    return this.experimentSpec.getName();
  }

  /**
   * The experiment instance name is the experiment name, prefixed by an execution date and time.
   * This is used to prefix all the file storage on AWS, such as case study artifacts and also generated
   * experiment results
   * @return String unique experiment instance name
   */
  public String getExperimentInstanceName() {

    if(this.experimentInstanceName == null) {
      var defaultExperimentName = this.getExperimentName().replaceAll("[-+.^:, ]","");
      var datetime = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(Calendar.getInstance().getTime());
      this.experimentInstanceName = String.format("%s-%s", datetime, defaultExperimentName);
    }

    return this.experimentInstanceName;

  }

  /**
   * The name of the bucket where the current experiment artifacts should be uploaded.
   * //TODO make this configurable from the DSL
   * @return the name of the bucket where the current exp artifacts are uploaded
   */
  public String getUploadLocation() {
    return this.BUCKET_NAME;
  }
}
