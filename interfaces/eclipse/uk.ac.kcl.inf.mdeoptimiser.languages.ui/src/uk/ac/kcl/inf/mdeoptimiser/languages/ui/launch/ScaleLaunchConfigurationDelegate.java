package uk.ac.kcl.inf.mdeoptimiser.languages.ui.launch;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import uk.ac.kcl.inf.mdeoptimiser.languages.ui.classpath.ScaleClasspathContainer;

public class ScaleLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
		IProgressMonitor monitor) throws CoreException {

		try {
			monitor.subTask(
				LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

			var runConfig = getVMRunnerConfiguration(configuration, mode, monitor);
			if (runConfig == null) {
				return;
			}

			// done the verification phase
			monitor.worked(1);

			monitor.subTask(
				LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);
			// Launch the configuration - 1 unit of work
			var runner = getVMRunner(configuration, mode);
			runner.run(runConfig, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Builds a VM runner configuration to which it appends the required 
	 * MDEOptimiser dependencies
	 * 
	 * @return a JVM configuration containing all the classpath and 
	 * argument values configured for MDEOptimiser
	 */
	public VMRunnerConfiguration getVMRunnerConfiguration(ILaunchConfiguration configuration, String mode,
		IProgressMonitor monitor) throws CoreException {

		monitor.beginTask(NLS.bind("{0}...", new String[] {configuration.getName()}), 3);

		// check for cancellation
		if (monitor.isCanceled()) {
			return null;
		}

		monitor.subTask(
			LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

		var workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}

		// Environment variables
		var envp = getEnvironment(configuration);

		// Program & VM arguments
		var pgmArgs = getStandaloneLauncherArguments(getConfiguredMoptPath(configuration));
		var vmArgs = concat(getVMArguments(configuration), getVMArguments(configuration, mode));
		var execArgs = new ExecutionArguments(vmArgs, pgmArgs);

		// VM-specific attributes
		var vmAttributesMap = getVMSpecificAttributesMap(configuration);

		// Bug 522333 :to be used for modulepath only for 4.7.*
		var paths = getMDEOClasspathAndModulePath(configuration);

		// Create VM config using MDEO RunConfiguration as main class
		var runConfig = new VMRunnerConfiguration(ScaleLaunchConfigurationAttributes.ATTR_SCALE_MAIN_CLASS_NAME,
			appendBundleDependenciesToClasspath(getClasspath(configuration)));
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setEnvironment(envp);
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		// current module name, if so
		try {
			var proj = JavaRuntime.getJavaProject(configuration);
			if (proj != null) {
				var module = (proj == null) ? null : proj.getModuleDescription();
				var modName = (module == null) ?  null : module.getElementName();
				if (modName != null) {
					runConfig.setModuleDescription(modName);
				}
			}
		} catch (CoreException e) {
			// Not a java Project so no need to set module description
		}

		if (!JavaRuntime.isModularConfiguration(configuration)) {
			// Bootpath
			runConfig.setBootClassPath(getBootpath(configuration));
		} else {
			// module path
			runConfig.setModulepath(paths[1]);
			if (!configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, true)) {
				runConfig.setOverrideDependencies(
					configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, ""));
			} else {
				runConfig.setOverrideDependencies(getModuleCLIOptions(configuration));
			}
		}
		// check for cancellation
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.worked(1);

		return runConfig;
	}

	/**
	 * Returns the program arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if no program
	 * arguments are specified.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the specified mopt configuration file path
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String getConfiguredMoptPath(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ScaleLaunchConfigurationAttributes.ATTR_SCALE_SOURCE_PATH, "");
	}

	/**
	 * Build the arguments to pass to the headless MDEOptimiser runner class.
	 * 
	 * @return headless arguments for running MDEOptimiser with the given mopt file
	 */
	public String getStandaloneLauncherArguments(String configuredMoptFile) {

		var arguments = "";

		var moptFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(configuredMoptFile));

		if (moptFile.exists()) {

			arguments += "-projectPath " + moptFile.getProject().getLocation().toOSString();
			arguments += " ";
			arguments += "-specPath " + moptFile.getRawLocation().toOSString();

			return arguments;
		}
		
		return null;
	}

	/**
	 * Returns the complete classpath of the launch configuration, including the Eclipse bundles
	 * as well as all the user configured dependencies.
	 * 
	 * @param configuration launch configuration
	 * @return the complete bundles classpath including eclipse jars and user configured classpath
	 */
	public String[] appendBundleDependenciesToClasspath(String[] classpathEntries) {

		LinkedHashSet<String> classpathEntriesSet = new LinkedHashSet<String>();

		if (classpathEntries != null) {
			classpathEntriesSet.addAll(Arrays.stream(classpathEntries).collect(Collectors.toList()));
		}

		var mdeoContainerBundles = ScaleClasspathContainer.BUNDLE_IDS_TO_INCLUDE;

		var bundleDependencies = new HashMap<Long, Bundle>();
		
		mdeoContainerBundles.forEach(bundle -> getBundleDependencies(bundleDependencies, Platform.getBundle(bundle)));
		
		bundleDependencies.values().stream().forEach(bundle -> {
						
			String file;
			try {
				file = FileLocator.getBundleFile(bundle).getCanonicalPath();
				classpathEntriesSet.add(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Is this bundle an unpackaged bundle, then add it's build location to the path
			if (ScaleClasspathContainer.buildFolderPath(bundle) != null) {
				classpathEntriesSet.add(ScaleClasspathContainer.buildFolderPath(bundle).toPortableString());
			}
		});

		return classpathEntriesSet.toArray(new String[classpathEntriesSet.size()]);
	}

	/**
	 * Recursively read the dependency model of a given bundle.
	 * 
	 * @return a map of the dependent bundle IDs and the loaded bundles.
	 */
	private HashMap<Long, Bundle> getBundleDependencies(HashMap<Long, Bundle> bundleDependencies, Bundle bundle) {

		if (bundle != null && !bundleDependencies.keySet().contains(bundle.getBundleId())) {

			bundleDependencies.put(bundle.getBundleId(), bundle);

			var dependencies = getBundleWiredDependencies(bundle);
			dependencies.forEach(d -> getBundleDependencies(bundleDependencies, d));
		}

		return bundleDependencies;
	}

	/**
	 * Fetches the dependent bundles from a given bundle. It looks both bundle 
	 * references as well as package references.
	 * 
	 * @return a list of dependent bundles, or an empty set
	 */
	private LinkedHashSet<Bundle> getBundleWiredDependencies(Bundle bundle) {

		var wiredBundles = new LinkedHashSet<Bundle>();
		var wiring = bundle.adapt(BundleWiring.class);

		if (wiring != null) {

			var wires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
			wires.forEach(wire -> wiredBundles.add(wire.getProviderWiring().getBundle()));
			
			wires = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
			wires.forEach(wire -> wiredBundles.add(wire.getProviderWiring().getBundle()));

		}

		return wiredBundles;

	}

	/**
	 * Fetches a classpath from the launch configuration and appends the dependency tree 
	 * of the resolved MDEOptimiser bundles.
	 * 
	 * This method is supposed to be used for cases where the JDK version supports modules.
	 * 
	 * @return an array containing classpath dependencies and module dependencies
	 */
	private String[][] getMDEOClasspathAndModulePath(ILaunchConfiguration config) throws CoreException {

		var classpathAndModulePath = getClasspathAndModulepath(config);

		return new String[][] {appendBundleDependenciesToClasspath(classpathAndModulePath[0]), classpathAndModulePath[1]};
	}

	private static String concat(String args1, String args2) {
		var args = new StringBuilder();

		if(args1 != null && !args1.isEmpty()) {
			args.append(args1);
		}

		if(args2 != null && !args2.isEmpty()) {
			args.append(" ");
			args.append(args2);
		}

		return args.toString();
	}
}
