package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.AbstractParser;
import uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.results.parser.MoeaProblem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JobResultsParser extends AbstractParser {

  private static final char DEFAULT_SEPARATOR = ',';
  private static final char DEFAULT_QUOTE = '"';

  public JobResultsParser(String experimentName, String model, String tool) {
    super(experimentName, model, tool);
    this.inferProblem();
  }

  public File getExperimentsPath() {
    return new File(String.format("%s/results/%s/%s", this.getExperimentName(), this.getModel(), this.getTool()));
  }

  /**
   * Analyse the generated results and infer the number of objectives and constraints
   * for the problem being solved.
   */
  public void inferProblem(){

    if(this.hasAccumulators()) {

      var accumulator = this.getAccumulators().values().stream().findFirst().get();

      if(accumulator.size("Population") > 0) {
        var solution = ((ArrayList<Solution>) accumulator.get("Population", 0)).get(0);

        //Infer the type of problem for these results by looking at one of the deserialised solutions
        this.setProblem(solution.getNumberOfObjectives(),
                solution.getNumberOfConstraints(), this.getExperimentName(), this.getModel());

      } else {
        throw new RuntimeException(String.format("Population size is 0 in accumulator for case study %s tool %s model %s",
                this.getExperimentName(), this.getTool(), this.getModel()));
      }

    } else {
      throw new RuntimeException(String.format("Unable to parse accumulators for case study %s tool %s model %s",
              this.getExperimentName(), this.getTool(), this.getModel()));
    }


  }

  @Override
  public void loadAccumulators() throws IOException {
    File root = this.getExperimentsPath();

    if (!root.exists()) {
      System.out.print("Could not find accumulators for path: " + root.getAbsolutePath());

      this.accumulators = new HashMap<Integer, Accumulator>();

      return;
    }

    Map<Integer, String> paths = new HashMap<Integer, String>();

    Files.walk(root.toPath())
        .filter(path -> !Files.isDirectory(path) && path.toString().endsWith("csv"))
        .forEach(
            path ->
                paths.put(
                    Integer.parseInt(path.getParent().getFileName().toString().replaceAll("[^\\d]", "")),
                    path.toAbsolutePath().toString()));

    Map<Integer, Accumulator> accumulators = new HashMap<Integer, Accumulator>();

    paths.forEach(
        (id, path) -> {
          accumulators.put(id, parseSerialisedBatchAccumulator(path));
          System.out.println("Processing path " + path);
        });

    this.accumulators = accumulators;
  }

  public void setProblem(MoeaProblem problem){
    this.problem = problem;
  }

  public void setProblem(int objectives, int constraints, String experimentInstanceName, String modelName){

    this.problem = new MoeaProblem(objectives, constraints, experimentInstanceName, modelName);

  }

  @Override
  public MoeaProblem getProblem() {

    if(this.problem == null) {
      this.inferProblem();
    }

    return this.problem;
  }

  @Override
  public String getUniqueName() {
    return String.format("%s_%s_%s", this.getExperimentName(), this.getTool(), this.getModel());
  }

  @Override
  public String getExperimentName() {
    return this.experimentName;
  }

  @Override
  public String getModel(){
    return this.model;
  }

  @Override
  public String getTool(){
    return this.tool;
  }

  public Accumulator parseSerialisedBatchAccumulator(String batchCSV) {

    CSVReader reader;

    Accumulator accumulator = new Accumulator();

    try {
      reader = new CSVReader(new FileReader(batchCSV), DEFAULT_SEPARATOR, DEFAULT_QUOTE);
      String[] line;

      String nfe = null;
      String elapsedTime = null;
      String approximationSet = null;
      String population = null;
      String populationSize = null;

      int row = 0;
      while ((line = reader.readNext()) != null) {

        if (row == 0) {
          // Load the headings
          nfe = line[0].trim();
          elapsedTime = line[1].trim();
          approximationSet = line[2].trim();
          population = line[3].trim();
          populationSize = line[4].trim();
        } else {
          accumulator.add(nfe, Integer.parseInt(line[0]));
          accumulator.add(elapsedTime, parseElapsedTime(line[1]));
          accumulator.add(approximationSet, parseSerialisedSolutions(line[2]));
          accumulator.add(population, parseSerialisedSolutions(line[3]));
          accumulator.add(populationSize, parsePopulationSize(line[4]));
        }

        row++;
      }

      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return accumulator;
  }

  public double parseElapsedTime(String line) {
    return Double.parseDouble(line.trim());
  }

  public int parsePopulationSize(String line) {
    return Integer.parseInt(line.trim());
  }

  public ArrayList<Solution> parseSerialisedSolutions(String serialisedSolution) {

    ArrayList<Solution> solutions = new ArrayList<Solution>();

    serialisedSolution = serialisedSolution.replaceAll(", ", "|").trim();

    String[] result = serialisedSolution.substring(1, serialisedSolution.length() - 1).split("\\|");

    for (String solutionVectors : result) {
      String[] variables = StringUtils.substringsBetween(solutionVectors, "[", "]");

      if (variables.length == 1) {
        String[] objectives = variables[0].split(",");

        Solution solution = new Solution(0, objectives.length);
        solution.setObjectives(parseDoublesArray(objectives));
        solutions.add(solution);
      } else {
        String[] objectives = variables[0].split(",");
        String[] constraints = variables[1].split(",");

        Solution solution = new Solution(0, objectives.length, constraints.length);

        solution.setConstraints(parseDoublesArray(constraints));
        solution.setObjectives(parseDoublesArray(objectives));
        solutions.add(solution);
      }
    }

    return solutions;
  }

  public double[] parseDoublesArray(String[] csvDoubles) {

    double[] original = new double[csvDoubles.length];

    for (int i = 0; i < csvDoubles.length; i++) {
      original[i] = Double.parseDouble(csvDoubles[i]);
    }
    return original;
  }
}
