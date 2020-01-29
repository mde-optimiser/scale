package uk.ac.kcl.inf.mdeoptimiser.languages.infrastructure.scale.executor;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.messaging.JobPayload;

import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class ToolExecutor {

  @Option(name="-payload",usage="Job specification payload.")
  private String payload;

  @Argument
  private List<String> arguments = new ArrayList<String>();

  public static void main(String[] args) {

    System.out.println(String.format("Passed arguments: %s", args));

    if(args.length == 0){
      //MDEO
      //args = new String[]{"-payload", "{\"job\":{\"task\":{\"className\":\"uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types.MDEOptimiser\",\"name\":\"MDEO\",\"modelName\":\"TTC_InputRDG_A\",\"experimentInstanceName\":\"2019-11-09-05-52-12-CRACaseStudy\",\"experimentName\":\"CRA Case Study\",\"taskFiles\":{\"2019-11-09-05-52-12-CRACaseStudy/TTC_InputRDG_A/MDEO/libraries/cra.jar\":{\"path\":\"libraries/cra.jar\"},\"2019-11-09-05-52-12-CRACaseStudy/TTC_InputRDG_A/MDEO/cra_model_a.mopt\":{\"path\":\"cra_model_a.mopt\"}},\"experimentArtifacts\":{\"2019-11-09-05-52-12-CRACaseStudy/src\":{\"path\":\"src\"}},\"id\":\"MDEO_a726cec4-8bfe-44cf-a0b7-16327386d003\",\"command\":\"cra_model_a.mopt\",\"taskDependencies\":{\"2019-11-09-05-52-12-CRACaseStudy/TTC_InputRDG_A/MDEO/libraries/cra.jar\":{\"path\":\"libraries/cra.jar\"}}},\"batchNumber\":1}}"};

      //MOMOT
      //args = new String[]{"-payload", "{\"job\":{\"task\":{\"className\":\"uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types.MOMoT\",\"name\":\"MOMOT\",\"modelName\":\"TTC_InputRDG_A\",\"experimentInstanceName\":\"2019-11-28-12-57-25-CRACaseStudy\",\"experimentName\":\"CRA Case Study\",\"taskFiles\":{\"2019-11-28-12-57-25-CRACaseStudy/TTC_InputRDG_A/MOMOT/libraries/cra-momot.jar\":{\"path\":\"libraries/cra-momot.jar\"},\"2019-11-28-12-57-25-CRACaseStudy/TTC_InputRDG_A/MOMOT/cra_model_a.momot\":{\"path\":\"cra_model_a.momot\"}},\"experimentArtifacts\":{\"2019-11-28-12-57-25-CRACaseStudy/src\":{\"path\":\"src\"}},\"id\":\"MOMoT_9b0dd395-8bd7-4b65-922c-a0669d6f06e3\",\"command\":\"cra_model_a.momot\",\"taskDependencies\":{\"2019-11-28-12-57-25-CRACaseStudy/TTC_InputRDG_A/MOMOT/libraries/cra-momot.jar\":{\"path\":\"libraries/cra-momot.jar\"}}},\"batchNumber\":2}}"};

      //SCALE
      args = new String[]{"payload", "{\"job\":{\"task\":{\"className\":\"uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.types.ResultsTask\",\"jobsLog\":\"2019-11-30-02-51-11-CRACaseStudySecond/results/mdeo-scale-jobs.json\",\"experimentInstanceName\":\"2019-11-30-02-51-11-CRACaseStudySecond\"},\"batchNumber\":-1}}"};
    }

    new ToolExecutor().run(args);
  }

  /**
   * Run the wrapper and begin loading the configured job tool.
   * @param args passed to the container running the wrapper
   */
  public void run(String args[]){

    //Parse parameters
    this.parseArguments(args);

    //Load the configured job
    var jobPayload = this.loadJob(this.payload);

    var job = jobPayload.getJob();

    //Download the job files inside the container

    //Run the job command
    System.out.println(String.format("Preparing to run job: %s", job.getName()));
    job.execute();

    //Upload the generated accumulator file and other results to S3
  }

  /**
   * Loads the passed arguments or prints an exception if the incorrect parameters have been given.
   * @param args
   */
  private void parseArguments(String args[]) {

    var parametersParser = new CmdLineParser(this);

    var outStream = System.out;

    try {

      // Parse arguments passed to the application
      parametersParser.parseArgument(args);

      outStream.println();
      outStream.println(String.format("Passed payload: -payload %s", payload));

      if( this.payload == null )
        throw new CmdLineException(parametersParser,"No argument is given");

    } catch( CmdLineException e ) {

      outStream.println();
      outStream.println(e.getMessage());
      outStream.println("java <wrapper-jar> [options...] arguments...");

      // print the list of available options
      parametersParser.printUsage(System.err);
      System.out.println();

      // print option sample. This is useful some time
      System.out.println("Example: java wrapper"+parametersParser.printExample(ALL));
    }
  }

  /**
   * Parse the given JSON into an instance of the IScaleJob interface.
   * @param jobJson
   * @return instance of the TaskPayload configuration passed to the wrapper.
   */
  private JobPayload loadJob(String jobJson){
    //TODO error checking here
    return JobPayload.deSerialiseJob(jobJson);
  }
}