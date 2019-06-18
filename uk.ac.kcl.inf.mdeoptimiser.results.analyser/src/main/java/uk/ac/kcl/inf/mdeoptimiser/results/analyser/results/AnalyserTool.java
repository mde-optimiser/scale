package uk.ac.kcl.inf.mdeoptimiser.results.analyser.results;

import java.util.ArrayList;
import org.eclipse.emf.common.util.EList;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Tool;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ToolConfiguration;

public class AnalyserTool {

  private Tool tool;
  private ArrayList<AnalyserToolConfiguration> configurations;

  public AnalyserTool(Tool tool) {

    this.setTool(tool);
    this.setConfigurations(tool.getConfigurations());
  }

  private void setConfigurations(EList<ToolConfiguration> configurations) {
    this.configurations = new ArrayList<>();
    configurations.forEach(c -> this.configurations.add(new AnalyserToolConfiguration(c)));
  }

  private void setTool(Tool tool) {
    this.tool = tool;
  }
}
