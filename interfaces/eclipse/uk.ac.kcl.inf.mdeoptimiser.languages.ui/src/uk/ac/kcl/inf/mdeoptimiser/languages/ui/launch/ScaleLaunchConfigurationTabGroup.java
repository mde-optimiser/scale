package uk.ac.kcl.inf.mdeoptimiser.languages.ui.launch;

import java.util.List;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

public class ScaleLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		var tabs = List.of(
			new ScaleSourceConfigurationTab(),
			new JavaClasspathTab(),
			new JavaJRETab(),
			new EnvironmentTab(),
			new CommonTab()
		);
		setTabs(tabs.toArray(new ILaunchConfigurationTab[tabs.size()]));
	}
}