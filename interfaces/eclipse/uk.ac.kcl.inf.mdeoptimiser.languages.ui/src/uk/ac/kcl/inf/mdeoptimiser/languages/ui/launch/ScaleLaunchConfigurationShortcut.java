package uk.ac.kcl.inf.mdeoptimiser.languages.ui.launch;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

public class ScaleLaunchConfigurationShortcut implements ILaunchShortcut {
	
	/**
	 * Launch MDEOptimiser from a file selection in Project Explorer.
	 */
	@Override
	public void launch(ISelection selection, String mode) {
		var moptFile = (IFile) ((IStructuredSelection)selection).getFirstElement();
		launch(moptFile, mode);
	}
	
	/**
	 * Launch MDEOptimiser from an editor right click -> Run as action
	 */
	@Override
	public void launch(IEditorPart editor, String mode) {
		var moptFile =  ResourceUtil.getFile(editor.getEditorInput());
		launch(moptFile, mode);
	}
	
	/**
	 * Launch a MDEOptimiser LaunchConfiguration using the given <code>moptFile</code> file
	 * reference. This will search for an already existing launch configuration for the 
	 * given file and if one is not found it will create one from scratch.
	 */
	public void launch(IFile moptFile, String mode){
		
		if(moptFile == null) {
			return;
		}
		
		var moptFilePath = moptFile.getFullPath().toString();
		var launchManager = DebugPlugin.getDefault().getLaunchManager();
		var launchConfigurationType = launchManager
				.getLaunchConfigurationType(ScaleLaunchConfigurationAttributes.SCALE_LAUNCH_CONFIGURATION_TYPE);
		
		try {
			
			var launchConfigurations = launchManager.getLaunchConfigurations(launchConfigurationType); 
			
			var launchConfiguration =  Arrays.stream(launchConfigurations).filter((Predicate<? super ILaunchConfiguration>) a -> {
				try {
					return a.getAttribute(ScaleLaunchConfigurationAttributes.ATTR_SCALE_SOURCE_PATH, "").equals(moptFilePath);
				} catch(Exception e) {
				}
				
				return false;
				
			}).collect(Collectors.toList()).get(0);
			
			if(launchConfiguration != null) {
				DebugUITools.launch((ILaunchConfiguration) launchConfiguration, mode);
				return;
			}
			
		} catch(CoreException e){
			return;
		}
		
		//Create a new launch configuration if one was not found
		try {
			
			var launchConfigurationWorkingCopy = launchConfigurationType.newInstance(null, moptFile.getName());
			launchConfigurationWorkingCopy.setAttribute(ScaleLaunchConfigurationAttributes.ATTR_SCALE_SOURCE_PATH, moptFilePath);
			setProjectOnClasspath(launchConfigurationWorkingCopy, moptFile);
			var launchConfiguration = launchConfigurationWorkingCopy.doSave();
			
			DebugUITools.launch(launchConfiguration, mode);
			
		} catch(CoreException e) {
			return;
		}
	}
	
	/**
	 * Adds the project of the current file as a project name attribute to the launch configuration.
	 * This is then added by the launch configuration to the classpath.
	 */
	public void setProjectOnClasspath(ILaunchConfigurationWorkingCopy configuration, IFile moptFile) {
		
		var moptFileJavaProject = JavaCore.create(moptFile.getProject());
		var javaProject = moptFileJavaProject.getJavaProject();

		if (javaProject != null && javaProject.exists()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, javaProject.getElementName());
		}	
	}

}
