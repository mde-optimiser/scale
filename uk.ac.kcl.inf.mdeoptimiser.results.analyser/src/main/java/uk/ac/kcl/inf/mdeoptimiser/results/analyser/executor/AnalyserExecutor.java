package uk.ac.kcl.inf.mdeoptimiser.results.analyser.executor;

import com.google.inject.Injector;
import com.google.inject.Provider;
import java.io.File;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import uk.ac.kcl.inf.mdeoptimiser.languages.MeerkatStandaloneSetupGenerated;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Analyser;
import uk.ac.kcl.inf.mdeoptimiser.results.analyser.results.AnalyserProblem;

public class AnalyserExecutor {

  private File projectPath;
  private File analyserFile;

  private static final Injector injector =
      new MeerkatStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();

  @Inject Provider<ResourceSet> resourceSetProvider;
  private List<AnalyserProblem> problems;

  public AnalyserExecutor(String projectPath, String analyserFile) {

    this.loadLogger();
  }

  /**
   * Provisional main method to start the Analysis
   *
   * @param args
   */
  public static void main(String[] args) {

    var analyser = AnalyserExecutor.injector.getInstance(AnalyserExecutor.class);

    analyser.start("", "");
  }

  public void start(String projectPath, String analyserFile) {

    try {

      this.setProjectPath(projectPath);
      this.setAnalyserFile(analyserFile);

      var analyserURI = URI.createFileURI(this.getAnalyserFile().getAbsolutePath());
      var analyserSpec = this.resourceSetProvider.get().getResource(analyserURI, true);

    } catch (Exception e) {
      Logger.error("Encountered exception: ", e);
    }
  }

  private void loadLogger() {
    Configurator.currentConfig().writer(new ConsoleWriter()).activate();
    Logger.info("Logger initialised. Writing logs to the console.");
  }

  private void setProblems(Analyser analyser) {

    analyser
        .getProblems()
        .forEach(
            p -> {
              this.problems.add(new AnalyserProblem(p));
            });
  }

  private void setProjectPath(String projectPath) throws Exception {
    this.projectPath = new File(projectPath);

    if (!this.projectPath.exists()) {
      throw new Exception(String.format("Analyser project path does not exist: %s", projectPath));
    }
  }

  private void setAnalyserFile(String analyserFile) throws Exception {
    this.analyserFile = new File(analyserFile);

    if (!this.projectPath.exists()) {
      throw new Exception(
          String.format("Invalid project configuration path given: %s", projectPath));
    }
  }

  public File getProjectPath() {
    return this.projectPath;
  }

  public File getAnalyserFile() {
    return this.analyserFile;
  }
}
