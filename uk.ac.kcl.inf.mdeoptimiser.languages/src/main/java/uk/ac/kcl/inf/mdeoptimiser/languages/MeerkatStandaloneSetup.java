/*
 * generated by Xtext 2.16.0
 */
package uk.ac.kcl.inf.mdeoptimiser.languages;


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
public class MeerkatStandaloneSetup extends MeerkatStandaloneSetupGenerated {

	public static void doSetup() {
		new MeerkatStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}
