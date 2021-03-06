package io.jddf.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class ValidatorTest {
  @Test
  public void testMaxDepth() {
    Gson gson = new Gson();
    Schema schema = gson.fromJson("{\"definitions\": {\"x\": {\"ref\": \"x\"}}, \"ref\": \"x\"}", Schema.class);

    Validator validator = new Validator();
    validator.setMaxDepth(3);
    assertThrows(MaxDepthExceededException.class, () -> validator.validate(schema, null));
  }

  @Test
  public void testMaxErrors() throws MaxDepthExceededException {
    Gson gson = new Gson();
    Schema schema = gson.fromJson("{\"elements\": {\"type\": \"string\"}}", Schema.class);
    JsonElement instance = gson.fromJson("[1, 1, 1, 1, 1]", JsonElement.class);

    Validator validator = new Validator();
    validator.setMaxErrors(3);
    assertEquals(3, validator.validate(schema, instance).size());
  }

  @TestFactory
  public List<DynamicTest> testSpec() throws UnsupportedEncodingException, MaxDepthExceededException {
    List<DynamicTest> tests = new ArrayList<>();
    tests.addAll(this.testSpecFile("spec/tests/validation/001-empty.json"));
    tests.addAll(this.testSpecFile("spec/tests/validation/002-ref.json"));
    tests.addAll(this.testSpecFile("spec/tests/validation/003-type.json"));
    tests.addAll(this.testSpecFile("spec/tests/validation/004-enum.json"));
    tests.addAll(this.testSpecFile("spec/tests/validation/005-elements.json"));
    tests.addAll(this.testSpecFile("spec/tests/validation/006-properties.json"));
    tests.addAll(this.testSpecFile("spec/tests/validation/007-values.json"));
    tests.addAll(this.testSpecFile("spec/tests/validation/008-discriminator.json"));

    return tests;
  }

  public List<DynamicTest> testSpecFile(String file) throws UnsupportedEncodingException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(file);
    Gson gson = new Gson();

    List<TestSuite> suites = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"),
        new TypeToken<List<TestSuite>>() {
        }.getType());

    List<DynamicTest> tests = new ArrayList<>();

    Validator validator = new Validator();
    for (TestSuite suite : suites) {
      int index = 0;
      for (TestCase testCase : suite.instances) {
        tests.add(DynamicTest.dynamicTest(file + "/" + suite.name + "/" + index, () -> {
          // Convert test case error into a parsed JSON Pointer. We here take
          // advantage of the fact that we don't actually need to support JSON
          // Pointer, just splitting on slash.
          List<ValidationError> expected = new ArrayList<>();
          for (TestCaseError error : testCase.errors) {
            ValidationError validationError = new ValidationError();
            validationError.setInstancePath(parseJSONPointer(error.instancePath));
            validationError.setSchemaPath(parseJSONPointer(error.schemaPath));

            expected.add(validationError);
          }

          List<ValidationError> actual = validator.validate(suite.schema, testCase.instance);

          expected.sort((e1, e2) -> String.join("", e1.getSchemaPath()).compareTo(String.join("", e2.getSchemaPath())));
          actual.sort((e1, e2) -> String.join("", e1.getSchemaPath()).compareTo(String.join("", e2.getSchemaPath())));

          assertEquals(expected, actual);
        }));

        index++;
      }
    }

    return tests;
  }

  // This is not a full-featured JSON Pointer parser. Such a parser isn't
  // necessary for the test suite, as it does not rely on JSON Pointer escaping.
  private static List<String> parseJSONPointer(String s) {
    if (s.isEmpty()) {
      return new ArrayList<>();
    } else {
      return Arrays.asList(s.substring(1).split("/"));
    }
  }

  private static class TestSuite {
    private String name;
    private Schema schema;
    private List<TestCase> instances;
  }

  private static class TestCase {
    private JsonElement instance;
    private List<TestCaseError> errors;
  }

  private static class TestCaseError {
    private String instancePath;
    private String schemaPath;
  }
}
