package io.jddf.gson;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

public class ValidatorTest {
  @Test
  public void testSpec() throws UnsupportedEncodingException, MaxDepthExceededException {
    this.testSpecFile("spec/tests/validation/001-empty.json");
    this.testSpecFile("spec/tests/validation/002-ref.json");
    this.testSpecFile("spec/tests/validation/003-type.json");
    this.testSpecFile("spec/tests/validation/004-enum.json");
    this.testSpecFile("spec/tests/validation/005-elements.json");
    this.testSpecFile("spec/tests/validation/006-properties.json");
    this.testSpecFile("spec/tests/validation/007-values.json");
    this.testSpecFile("spec/tests/validation/008-discriminator.json");
  }

  public void testSpecFile(String file) throws UnsupportedEncodingException, MaxDepthExceededException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(file);
    Gson gson = new Gson();

    List<TestSuite> suites = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"),
        new TypeToken<List<TestSuite>>() {
        }.getType());

    System.out.println(suites);

    Validator validator = new Validator();
    for (TestSuite suite : suites) {
      System.out.println(suite.name);
      for (TestCase testCase : suite.instances) {
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

        System.out.println(expected);
        System.out.println(suite.schema);
        System.out.println(testCase.instance);

        List<ValidationError> actual = validator.validate(suite.schema, testCase.instance);

        expected.sort((e1, e2) -> String.join("", e1.getSchemaPath()).compareTo(String.join("", e2.getSchemaPath())));
        actual.sort((e1, e2) -> String.join("", e1.getSchemaPath()).compareTo(String.join("", e2.getSchemaPath())));

        assertEquals(expected, actual);
      }
    }
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
