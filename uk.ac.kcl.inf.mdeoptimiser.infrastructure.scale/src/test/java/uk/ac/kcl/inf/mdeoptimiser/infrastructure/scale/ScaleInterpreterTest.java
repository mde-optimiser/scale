package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import uk.ac.kcl.inf.mdeoptimiser.languages.ScaleStandaloneSetup;
import uk.ac.kcl.inf.mdeoptimiser.languages.scale.Scale;


public class ScaleInterpreterTest {

  Injector injector = new ScaleStandaloneSetup().createInjectorAndDoEMFRegistration();

  ParseHelper<Scale> parseHelper;
  ValidationTestHelper validationHelper;
  String pathPrefix;

  @BeforeEach
  public void initialiseParserHelper() {
    parseHelper = injector.getInstance(new Key<ParseHelper<Scale>>() {});
    validationHelper = new ValidationTestHelper();
    pathPrefix = "gen/";
  }


  @Test
  public void testLanguageSyntax(TestInfo testInfo) throws Exception {

//    var modelSpec =
//            String.join(
//                    System.getProperty("line.separator"),
//                    "infrastructure \"aws batch\" {",
//                    "type aws",
//                    "account \"default\"",
//                    "}",
//                    "experiment \"test name\" {",
//                      "task \"mdeoptimiser\" {",
//                        "run \"rulegen.mopt\"",
//                        "dependencies \"pom.xml\"",
//                      "}",
//                    "}");
//
//    var model = parseHelper.parse(modelSpec);
//
//    runTestSearch(model, testInfo);
  }

//  @Test
//  public void experimentsConfigurationTest(TestInfo testInfo) throws Exception {
//
//    var modelSpec =
//            String.join(
//                    System.getProperty("line.separator"),
//                    "infrastructure \"aws batch\" {",
//                    "type aws",
//                    "account \"default\"",
//                    "}",
//                    "experiment \"test name\" {",
//                    "task \"mdeoptimiser\" {",
//                    "run \"rulegen.mopt\"",
//                    "dependencies \"pom.xml\"",
//                    "}",
//                    "task \"mdeoptimiser\" {",
//                    "run \"rulegen.mopt\"",
//                    "dependencies \"pom.xml\"",
//                    "}",
//                    "}");
//
//    var model = parseHelper.parse(modelSpec);
//
//    var scaleInterpreter = new ScaleInterpreter(this.pathPrefix, model);
//
//    var experiments = scaleInterpreter.getExperiments();
//
//    Assert.assertEquals(1, experiments.size());
//    Assert.assertEquals(2, experiments.get(0).getTasks().size());
//  }

  /**
   * Helper method to run the MOPT configurations
   *
   * @param model instance of a parsed MOPT file
   * @param testInfo instance of the running test function
   */
  private void runTestSearch(Scale model, TestInfo testInfo) {
    Assertions.assertNotNull(model);
    validationHelper.assertNoErrors(model);

  }

}
