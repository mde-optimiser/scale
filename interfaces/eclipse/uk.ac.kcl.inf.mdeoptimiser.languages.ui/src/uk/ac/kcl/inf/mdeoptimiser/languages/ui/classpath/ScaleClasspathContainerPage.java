package uk.ac.kcl.inf.mdeoptimiser.languages.ui.classpath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ScaleClasspathContainerPage implements IClasspathContainerPage {
	
	@Override
	public boolean finish() { return true; }
	
	@Override
	public IClasspathEntry getSelection() {
		return JavaCore.newContainerEntry(new ScaleClasspathContainer().getPath());
	}
	
	@Override
	public void setSelection(IClasspathEntry selection) { }
	
	@Override
	public boolean canFlipToNextPage() { return false; }
	
	@Override
	public String getName() {
		return "MDEOptimiser DSL Libraries";
	}
	
	@Override
	public IWizardPage getNextPage() { return null; }

	IWizardPage previous = null;
	
	@Override
	public void setPreviousPage(IWizardPage previous) {
		this.previous = previous;
	}

	@Override
	public org.eclipse.jface.wizard.IWizardPage getPreviousPage() { return previous; }
	
	IWizard wizard = null;
	
	@Override
	public void setWizard(IWizard wizard) {
		this.wizard = wizard;
	}
	
	@Override
	public org.eclipse.jface.wizard.IWizard getWizard() { return wizard; }
	
	@Override
	public boolean isPageComplete() { return true; }
	
	Label control = null;
	
	@Override
	public void createControl(Composite comp) {
		control = new Label(comp, SWT.WRAP);
		control.setText("The MDEOptimiser DSL Libraries container adds jars required for running searches with custom fitness functions in the DSL.");
	}
	
	@Override
	public Control getControl() { return control; }
	
	@Override
	public void dispose() {
		control.dispose();
	}
	
	@Override
	public String getDescription() {
		return "Information about the MDEOptimiser DSL Libraries";
	}
	
	@Override
	public String getErrorMessage() { return null; }
	
	@Override
	public  Image getImage() { return null; }
	
	@Override
	public String getMessage() { return null; }
	
	@Override
	public String getTitle() {
		return "MDEOptimiser DSL Libraries";
	}
	
	@Override
	public void performHelp() {}
	
	@Override
	public void setDescription(String description) {}
	
	@Override
	public void setImageDescriptor(ImageDescriptor image) {}
	
	@Override
	public void setTitle(String title) {}
	
	@Override
	public void setVisible(boolean visible) { }
}
