package uk.ac.kcl.inf.mdeoptimiser.results.analyser.results;

import java.util.ArrayList;
import java.util.List;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Model;

public class AnalyserModel {

  private Model model;
  private List<AnalyserTool> tools;

  public AnalyserModel(Model model) {
    this.setModel(model);
    this.setTools();
  }

  private void setTools() {
    this.tools = new ArrayList<>();
    this.model.getTools().forEach(tool -> tools.add(new AnalyserTool(tool)));
  }

  private void setModel(Model model) {
    this.model = model;
  }

  public String getName() {
    return this.model.getName();
  }

  public List<AnalyserTool> getTools() {
    return this.tools;
  }
}
