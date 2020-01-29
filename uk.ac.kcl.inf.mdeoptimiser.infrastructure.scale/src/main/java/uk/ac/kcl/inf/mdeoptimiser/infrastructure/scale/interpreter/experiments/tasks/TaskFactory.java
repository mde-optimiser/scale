package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks;

import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types.MDEOptimiser;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types.MOMoT;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types.ResultsTask;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Task;

public class TaskFactory {

    public IScaleTask getTask(ExperimentConfiguration experimentConfiguration, Model model, Task taskSpec){

      var taskType = this.getTaskType(taskSpec);

      //Convert the specified taskSpec to the corresponding type object
      switch(taskType) {

        case MDEO:
          return new MDEOptimiser(experimentConfiguration, model, taskSpec);

        case MOMoT:
          return new MOMoT(experimentConfiguration, model, taskSpec);

//        case RESULTS:
//          return new ResultsTask(experimentConfiguration);

        default:
          throw new RuntimeException(String.format("Unknown task type %s", taskSpec.getRun().getCommand()));
      }
    }

  /**
   * Determine the type of the task from the DSL specification
   * @param taskSpec
   * @return TaskType indicating the type of the current task
   */
  private TaskType getTaskType(Task taskSpec){

    if(taskSpec.getRun().getCommand().endsWith(".mopt")) {
      return TaskType.MDEO;
    }

    if(taskSpec.getRun().getCommand().endsWith(".momot")) {
      return TaskType.MOMoT;
    }


    throw new RuntimeException(String.format("Unknown task type %s", taskSpec.getRun().getCommand()));
  }
}