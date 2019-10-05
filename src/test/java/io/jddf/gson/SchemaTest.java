package io.jddf.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class SchemaTest {
  // We ignore these spec test cases because Gson's behavior in many cases is to
  // either type-cast or ignore properties which have the wrong type.
  //
  // Broadly speaking, these are test cases that it would be un-Java-like, or
  // un-Gson-like, to attempt to handle.
  private static final List<String> IGNORED_SPEC_TESTS = Arrays.asList("type not a string",
      "type not a pre-defined value", "enum not an array of strings", "enum contains repated values",
      "elements not a schema", "property not a schema", "optional property not a schema", "values is not a schema",
      "discriminator tag is not a string", "definition not a schema", "additionalProperties not a boolean");

  @Test
  public void testJSONRoundtrip() {
    String schemaJSON = "{\"ref\":\"a\",\"type\":\"uint32\",\"enum\":[\"b\",\"c\"],\"elements\":{\"ref\":\"x\"},\"properties\":{\"d\":{\"ref\":\"x\"}},\"optionalProperties\":{\"e\":{\"ref\":\"x\"}},\"values\":{\"ref\":\"x\"},\"discriminator\":{\"tag\":\"f\",\"mapping\":{\"g\":{\"ref\":\"x\"}}}}";
    Gson gson = new Gson();
    Schema schema = gson.fromJson(schemaJSON, Schema.class);

    assertEquals(schemaJSON, gson.toJson(schema));
  }

  @TestFactory
  public List<DynamicTest> testVerify() throws UnsupportedEncodingException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("spec/tests/invalid-schemas.json");
    Gson gson = new Gson();

    List<TestCase> testCases = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"),
        new TypeToken<List<TestCase>>() {
        }.getType());

    List<DynamicTest> tests = new ArrayList<>();
    for (TestCase testCase : testCases) {
      tests.add(DynamicTest.dynamicTest(testCase.name, () -> {
        assumeFalse(IGNORED_SPEC_TESTS.contains(testCase.name));

        try {
          Schema schema = gson.fromJson(testCase.schema, Schema.class);
          if (schema == null) {
            // This is trivially an invalid schema.
            return;
          }

          schema.verify();
        } catch (JsonSyntaxException | InvalidSchemaException e) {
          // These are the two errors we consider acceptable in this situation.
          // The test has passed if we reach this code.
          return;
        }

        // No error was raised. The invalid schema was not detected.
        fail();
      }));
    }

    return tests;
  }

  @Test
  public void testGetForm() {
    Schema schema = new Schema();
    assertEquals(Form.EMPTY, schema.getForm());

    schema = new Schema();
    schema.setRef("");
    assertEquals(Form.REF, schema.getForm());

    schema = new Schema();
    schema.setType(Type.BOOLEAN);
    assertEquals(Form.TYPE, schema.getForm());

    schema = new Schema();
    schema.setEnum(Set.of(""));
    assertEquals(Form.ENUM, schema.getForm());

    schema = new Schema();
    schema.setElements(new Schema());
    assertEquals(Form.ELEMENTS, schema.getForm());

    schema = new Schema();
    schema.setProperties(Map.of("", new Schema()));
    assertEquals(Form.PROPERTIES, schema.getForm());

    schema = new Schema();
    schema.setOptionalProperties(Map.of("", new Schema()));
    assertEquals(Form.PROPERTIES, schema.getForm());

    schema = new Schema();
    schema.setValues(new Schema());
    assertEquals(Form.VALUES, schema.getForm());

    schema = new Schema();
    schema.setDiscriminator(new Discriminator());
    assertEquals(Form.DISCRIMINATOR, schema.getForm());
  }

  private static class TestCase {
    private String name;
    private JsonElement schema;
  }
}
