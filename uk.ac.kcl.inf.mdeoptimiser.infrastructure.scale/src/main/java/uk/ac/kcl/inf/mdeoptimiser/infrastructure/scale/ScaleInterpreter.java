package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.ExperimentConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.experiments.tasks.IResultsProcessingTask;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.IProvider;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.ProviderFactory;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.generator.JobsGenerator;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.jobs.scheduler.JobsScheduler;
import uk.ac.kcl.inf.mdeoptimiser.languages.ScaleStandaloneSetup;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Scale;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class ScaleInterpreter {

  private Path basepath;
  private Scale model;
  private List<ExperimentConfiguration> experiments;
  private IProvider infrastructureProvider;
  private JobsGenerator jobsGenerator = new JobsGenerator();
  private JobsScheduler jobsScheduler;

  static Injector injector =  new ScaleStandaloneSetup().createInjectorAndDoEMFRegistration();

  @Inject
  Provider<ResourceSet> resourceSetProvider;

  @Option(name="-projectPath",usage="Tool base path used to load artifacts defined in the DSL.")
  private String projectPath;

  @Option(name="-specPath",usage="Specification file name to execute.")
  private String specPath;

  public static void main(String args[]) {

    var app = injector.getInstance(ScaleInterpreter.class);
    app.run(args);
  }

  /**
   * Parse the arguments and initialise the tool
   * @param args
   */
  public void run(String args[]) {

    //var arr = new String[]{"-projectPath", "./", "-specPath", "scale.sc"};

    //Parse parameters
    this.parseArguments(args);

    if( this.specPath != null && this.projectPath != null ) {
      //Run the tool
      start(this.projectPath, this.specPath);
    }
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

      if( this.specPath == null && this.projectPath == null )
        throw new CmdLineException(parametersParser,"MDEO Scale usage instructions");


      if( this.projectPath == null )
        throw new CmdLineException(parametersParser,"Missing -projectPath parameter.");


      if( this.specPath == null )
        throw new CmdLineException(parametersParser,"Missing -specPath parameter.");

      outStream.println();
      outStream.println(String.format("Passed root path: -projectPath %s", this.projectPath));
      outStream.println(String.format("Passed specification path: -specPath %s", this.specPath));

    } catch( CmdLineException e ) {

      outStream.println();
      outStream.println(e.getMessage());

      outStream.println();
      outStream.println("java -jar scale.jar [options...] arguments...");

      // print the list of available options
      parametersParser.printUsage(System.out);
      System.out.println();

      // print option sample. This is useful some time
      System.out.println("Example: java -jar scale.jar "+parametersParser.printExample(ALL));
    }
  }

  /**
   * Perform some basic input validation.
   * TODO: This should really use an arguments parser with a better validation approach
   * @param basePath
   * @param scaleSpecPath
   */
  public void inputValidation(String basePath, String scaleSpecPath){
    //Do some validation and error prompting
    if(basePath == null){
      System.out.println(String.format("Provided basepath is empty: %s", basePath));
      System.exit(-1);
    }

    if(scaleSpecPath == null){
      System.out.println(String.format("Provided scale spec file path is empty: %s", basePath));
      System.exit(-1);
    }

    if(resourceSetProvider == null){
      System.out.println("Injected resource set provider is null. Invalid StandaloneSetup initialisation");
      System.exit(-1);
    }

  }

  /**
   * Application entry point. Try parsing the specified DSL file and start the application
   * @param basePath
   * @param scaleSpecPath
   */
  public void start(String basePath, String scaleSpecPath){

    //Do some very basic validation
    this.inputValidation(basePath, scaleSpecPath);

    //Parse the DSL
    var scaleSpec = new File(scaleSpecPath);
    if(!scaleSpec.exists()){
      System.out.println(String.format("Could not load Scale spec file: %s", scaleSpecPath));
      System.exit(-1);
    }

    var scaleSpecResource = resourceSetProvider.get().getResource(URI.createFileURI(scaleSpec.getAbsolutePath()), true);
    var scaleDslModel = (Scale) scaleSpecResource.getContents().get(0);

    if(scaleDslModel == null){
      System.out.println(String.format("Could not load Scale spec model from the resource set."));
      System.exit(-1);
    }

    //Set parameters
    this.setModel(scaleDslModel);
    this.setProjectBasepath(basePath);

    //Let's play
    start();
  }

  /**
   * Start method of the application. Initialises all the components and runs them.
   */
  public void start() {

    //TODO This is mainly implemented for AWS at the moment
    //Do we have any valid experiments to run
    //Parse experiments
    var experimentsConfigurations = getExperiments();

      //For each experiment load the tasks and check the dependencies on disk, statically
      // Build compute environment, default storage location, job queues and task definitions
    if(!experiments.isEmpty()) {

      //Initialise the infrastructure

      //initialiseInfrastructure();

      //for each experiment
      for(var experimentConfiguration : experimentsConfigurations) {
        //Generate the experiment S3 prefix

        //create s3 location with artifacts and upload the experiment artifacts to it
        // under date-experiment/artifacts/
        //For each task, create a subdirectory in the experiments path and upload the spec and the dependencies
        // under date-experiment/model/task/dependencies/
        // under date-experiment/model/task/spec.ext
        var uploadLocation = this.getInfrastructureProvider().uploadFiles(experimentConfiguration);

        //Now we should have all the files on S3

        //Create job definitions for the registered task types
        this.getInfrastructureProvider().createJobDefinitions(experimentConfiguration.getExperimentName(), experimentConfiguration.getTasks());

        //Generate jobs
        var configuredJobs = jobsGenerator.getExperimentJobs(experimentConfiguration);

        //Schedule jobs on AWS
        this.getJobsScheduler().scheduleExperimentJobs(configuredJobs);

        //Prepare results processing job
        var resultProcessingJob = jobsGenerator.getResultsAnalysisJobs(experimentConfiguration, configuredJobs);

        this.getInfrastructureProvider().createJobDefinitions(experimentConfiguration.getExperimentName(), List.of(resultProcessingJob.getTask()));

        var resultsProcessingTask = (IResultsProcessingTask) resultProcessingJob.getTask();

        //Bypass dependent jobs limit on batch. Has some disadvantages
        this.getInfrastructureProvider().uploadFiles(resultsProcessingTask.getScheduledJobsLog());

        //Schedule results processing job
        this.getJobsScheduler().scheduleExperimentJobs(List.of(resultProcessingJob));
      }


    }
  }

  /**
   * Make a call to the infrastructure provider and use the user specified infrastructure details
   * to configure / make available a new environment for running the specified experiments.
   */
  public void initialiseInfrastructure() {

    this.getInfrastructureProvider().build();
  }

  public IProvider getInfrastructureProvider() {

    if(this.infrastructureProvider == null){
      this.infrastructureProvider =  ProviderFactory.get(this.model.getInfrastructure());
    }

    return this.infrastructureProvider;
  }

  /**
   * Get a list of configured experiments in the DSL
   * @return list of specified experiment configurations
   */
  public List<ExperimentConfiguration> getExperiments(){

    if(this.experiments == null){
      this.setExperiments(this.model);
    }

    return this.experiments;
  }

  /**
   * Load configured experiments from the DSL
   * @param model
   */
  private void setExperiments(Scale model) {
    this.experiments = model.getExperiments()
            .stream()
            .map(ExperimentConfiguration::new)
            .collect(Collectors.toList());
  }

  /**
   * Set the current instance of the scale model.
   * @param scale
   */
  private void setModel(Scale scale) {
    this.model = scale;
  }

  /**
   * Set the root path from where the scale model is executed.
   * @param basepath
   */
  private void setProjectBasepath(String basepath) {

    this.basepath = Paths.get(basepath);
  }

  public JobsScheduler getJobsScheduler(){

    if(this.jobsScheduler == null){
      var infrastructureProvider = this.getInfrastructureProvider();
      this.jobsScheduler = new JobsScheduler(infrastructureProvider);
    }

    return this.jobsScheduler;
  }

}