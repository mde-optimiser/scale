package uk.ac.kcl.inf.mdeoptimiser.languages.ui.classpath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.core.runtime.Path;
import uk.ac.kcl.inf.mdeoptimiser.languages.ui.classpath.ScaleClasspathContainer;

/**
 * Classpath container initialiser for the MDEOptimiser classpath container. Implements the Eclipse extension point.  
 */
public class ScaleClasspathContainerInitializer extends ClasspathContainerInitializer {
	

	public static final Path SCALE_LIBRARY_PATH = new Path("uk.ac.kcl.inf.mdeoptimiser.languages.ui.Scale.SCALE_CONTAINER");

	@Override
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		JavaCore.setClasspathContainer(
			containerPath,
			new IJavaProject[] {project},
			new IClasspathContainer[] {new ScaleClasspathContainer()}, 
			null);
	}
	
}
