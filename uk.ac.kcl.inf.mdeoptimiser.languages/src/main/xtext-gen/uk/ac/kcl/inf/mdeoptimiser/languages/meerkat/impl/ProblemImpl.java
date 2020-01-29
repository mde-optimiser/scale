/**
 * generated by Xtext 2.18.0
 */
package uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.AnalysisConfiguration;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.MeerkatPackage;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Model;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.Problem;
import uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.ProblemConfiguration;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Problem</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.impl.ProblemImpl#getName <em>Name</em>}</li>
 *   <li>{@link uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.impl.ProblemImpl#getConfiguration <em>Configuration</em>}</li>
 *   <li>{@link uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.impl.ProblemImpl#getModels <em>Models</em>}</li>
 *   <li>{@link uk.ac.kcl.inf.mdeoptimiser.languages.meerkat.impl.ProblemImpl#getAnalysis <em>Analysis</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ProblemImpl extends MinimalEObjectImpl.Container implements Problem
{
  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected String name = NAME_EDEFAULT;

  /**
   * The cached value of the '{@link #getConfiguration() <em>Configuration</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getConfiguration()
   * @generated
   * @ordered
   */
  protected ProblemConfiguration configuration;

  /**
   * The cached value of the '{@link #getModels() <em>Models</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getModels()
   * @generated
   * @ordered
   */
  protected EList<Model> models;

  /**
   * The cached value of the '{@link #getAnalysis() <em>Analysis</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getAnalysis()
   * @generated
   * @ordered
   */
  protected AnalysisConfiguration analysis;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ProblemImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return MeerkatPackage.Literals.PROBLEM;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(String newName)
  {
    String oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MeerkatPackage.PROBLEM__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ProblemConfiguration getConfiguration()
  {
    return configuration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetConfiguration(ProblemConfiguration newConfiguration, NotificationChain msgs)
  {
    ProblemConfiguration oldConfiguration = configuration;
    configuration = newConfiguration;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MeerkatPackage.PROBLEM__CONFIGURATION, oldConfiguration, newConfiguration);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setConfiguration(ProblemConfiguration newConfiguration)
  {
    if (newConfiguration != configuration)
    {
      NotificationChain msgs = null;
      if (configuration != null)
        msgs = ((InternalEObject)configuration).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MeerkatPackage.PROBLEM__CONFIGURATION, null, msgs);
      if (newConfiguration != null)
        msgs = ((InternalEObject)newConfiguration).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MeerkatPackage.PROBLEM__CONFIGURATION, null, msgs);
      msgs = basicSetConfiguration(newConfiguration, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MeerkatPackage.PROBLEM__CONFIGURATION, newConfiguration, newConfiguration));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Model> getModels()
  {
    if (models == null)
    {
      models = new EObjectContainmentEList<Model>(Model.class, this, MeerkatPackage.PROBLEM__MODELS);
    }
    return models;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AnalysisConfiguration getAnalysis()
  {
    return analysis;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetAnalysis(AnalysisConfiguration newAnalysis, NotificationChain msgs)
  {
    AnalysisConfiguration oldAnalysis = analysis;
    analysis = newAnalysis;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MeerkatPackage.PROBLEM__ANALYSIS, oldAnalysis, newAnalysis);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setAnalysis(AnalysisConfiguration newAnalysis)
  {
    if (newAnalysis != analysis)
    {
      NotificationChain msgs = null;
      if (analysis != null)
        msgs = ((InternalEObject)analysis).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MeerkatPackage.PROBLEM__ANALYSIS, null, msgs);
      if (newAnalysis != null)
        msgs = ((InternalEObject)newAnalysis).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MeerkatPackage.PROBLEM__ANALYSIS, null, msgs);
      msgs = basicSetAnalysis(newAnalysis, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MeerkatPackage.PROBLEM__ANALYSIS, newAnalysis, newAnalysis));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case MeerkatPackage.PROBLEM__CONFIGURATION:
        return basicSetConfiguration(null, msgs);
      case MeerkatPackage.PROBLEM__MODELS:
        return ((InternalEList<?>)getModels()).basicRemove(otherEnd, msgs);
      case MeerkatPackage.PROBLEM__ANALYSIS:
        return basicSetAnalysis(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case MeerkatPackage.PROBLEM__NAME:
        return getName();
      case MeerkatPackage.PROBLEM__CONFIGURATION:
        return getConfiguration();
      case MeerkatPackage.PROBLEM__MODELS:
        return getModels();
      case MeerkatPackage.PROBLEM__ANALYSIS:
        return getAnalysis();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case MeerkatPackage.PROBLEM__NAME:
        setName((String)newValue);
        return;
      case MeerkatPackage.PROBLEM__CONFIGURATION:
        setConfiguration((ProblemConfiguration)newValue);
        return;
      case MeerkatPackage.PROBLEM__MODELS:
        getModels().clear();
        getModels().addAll((Collection<? extends Model>)newValue);
        return;
      case MeerkatPackage.PROBLEM__ANALYSIS:
        setAnalysis((AnalysisConfiguration)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case MeerkatPackage.PROBLEM__NAME:
        setName(NAME_EDEFAULT);
        return;
      case MeerkatPackage.PROBLEM__CONFIGURATION:
        setConfiguration((ProblemConfiguration)null);
        return;
      case MeerkatPackage.PROBLEM__MODELS:
        getModels().clear();
        return;
      case MeerkatPackage.PROBLEM__ANALYSIS:
        setAnalysis((AnalysisConfiguration)null);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case MeerkatPackage.PROBLEM__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case MeerkatPackage.PROBLEM__CONFIGURATION:
        return configuration != null;
      case MeerkatPackage.PROBLEM__MODELS:
        return models != null && !models.isEmpty();
      case MeerkatPackage.PROBLEM__ANALYSIS:
        return analysis != null;
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (name: ");
    result.append(name);
    result.append(')');
    return result.toString();
  }

} //ProblemImpl