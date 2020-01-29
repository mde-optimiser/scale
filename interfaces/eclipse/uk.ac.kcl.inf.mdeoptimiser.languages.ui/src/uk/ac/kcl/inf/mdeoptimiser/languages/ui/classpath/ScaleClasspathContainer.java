package uk.ac.kcl.inf.mdeoptimiser.languages.ui.classpath;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.core.IClasspathEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import java.io.IOException;

/**
 * MDEOptimiser classpath container.
 */
public class ScaleClasspathContainer implements IClasspathContainer {
	
	
	static final String UK_AC_KCL_MDEO_LANGUAGES_SCALE_BUNDLE_ID = "uk.ac.kcl.inf.mdeoptimiser.languages";
	static final String UK_AC_KCL_MDEO_ECLIPSE_UI_BUNDLE_ID = "uk.ac.kcl.inf.mdeoptimiser.languages.ui";
	static final String UK_AC_KCL_MDEO_INTERFACE_CLI_BUNDLE_ID = "uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale";
	public static final IPath CONTAINER_PATH = ScaleClasspathContainerInitializer.SCALE_LIBRARY_PATH.append("/dsl");
	
	
	public static final List<String> BUNDLE_IDS_TO_INCLUDE = List.of("org.eclipse.emf.ecore", 
															"org.eclipse.emf.common", 
															UK_AC_KCL_MDEO_LANGUAGES_SCALE_BUNDLE_ID, 
															UK_AC_KCL_MDEO_ECLIPSE_UI_BUNDLE_ID,
															UK_AC_KCL_MDEO_INTERFACE_CLI_BUNDLE_ID);
	
	ArrayList<IClasspathEntry> classpathEntries;
	
	@Override
	public IClasspathEntry[] getClasspathEntries() {
		
		if(this.classpathEntries == null) {
			this.classpathEntries = new ArrayList<IClasspathEntry>();
			ScaleClasspathContainer.BUNDLE_IDS_TO_INCLUDE.stream().forEach(value -> addEntry(this.classpathEntries, value));
					
		}
		
		return this.classpathEntries.toArray(new ClasspathEntry[this.classpathEntries.size()]);
	}
	
	@Override
	public String getDescription() {
		return "MDEO Scale DSL Libraries";
	}
		
	@Override
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}
	
	@Override
	public IPath getPath() { 
		return CONTAINER_PATH;
	}
	
	private void addEntry(List<IClasspathEntry> classpathEntries, String bundleId){
	
		var bundle = Platform.getBundle(bundleId);
		
		if(bundle != null) {
			
			//We may need to add target/classes to the bundle path if in Debug mode
			if(buildFolderPath(bundle) != null) {
				classpathEntries.add(JavaCore.newLibraryEntry(buildFolderPath(bundle), null, null, false));
			}
				
			classpathEntries.add(JavaCore.newLibraryEntry(getBundlePath(bundle), null, null, false));
		}
	}
	
	private IPath getBundlePath(Bundle bundle) {
		
		var path = buildFolderPath(bundle);
		
		if(path == null) {
			
			//No need to deal with the target/classes case, so adding a normal JAR
			try {
				return new Path(FileLocator.getBundleFile(bundle).getCanonicalPath());
			} catch (IOException e) {
				System.out.println("Can't resolve path '" + bundle.getSymbolicName() + "'");
			}	
		}
		
		return path;
	}
	
	/**
	 * For cases when debugging inside Eclipse, the bundle is compiled to target/classes
	 * and by default this folder is not recognised by the classpath loader. This is a fix that
	 * adds the required path suffix to make sure the plugin can be used correctly when debugging.
	 * 
	 */
	public static IPath buildFolderPath(Bundle bundle) {
		
		var buildFolderURL = FileLocator.find(bundle, new Path("target/classes"), null);
		
		if(buildFolderURL != null) {
			try {
				var buildFolderFileURL = FileLocator.toFileURL(buildFolderURL);
				
				return new Path(buildFolderFileURL.getPath()).makeAbsolute();
				
			} catch (IOException e) {
				//TODO Logger
				System.out.println("Can't resolve path '" + bundle.getSymbolicName() + "'");
			}
		}
		
		return null;
	}
	
}
