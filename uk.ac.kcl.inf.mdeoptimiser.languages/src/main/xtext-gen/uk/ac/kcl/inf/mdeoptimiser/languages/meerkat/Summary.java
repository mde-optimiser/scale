/**
 * generated by Xtext 2.18.0
 */
package uk.ac.kcl.inf.mdeoptimiser.languages.meerkat;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Summary</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Summary#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @see uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.MeerkatPackage#getSummary()
 * @model
 * @generated
 */
public interface Summary extends EObject
{
  /**
   * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
   * The list contents are of type {@link uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Parameter}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Parameters</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Parameters</em>' containment reference list.
   * @see uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.MeerkatPackage#getSummary_Parameters()
   * @model containment="true"
   * @generated
   */
  EList<Parameter> getParameters();

} // Summary
