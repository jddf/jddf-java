package io.jddf.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

public class SchemaTest {
  @Test
  public void testJSONRoundtrip() {
    String schemaJSON = "{\"ref\":\"a\",\"type\":\"uint32\",\"enum\":[\"b\",\"c\"],\"elements\":{\"ref\":\"x\"},\"properties\":{\"d\":{\"ref\":\"x\"}},\"optionalProperties\":{\"e\":{\"ref\":\"x\"}},\"values\":{\"ref\":\"x\"},\"discriminator\":{\"tag\":\"f\",\"mapping\":{\"g\":{\"ref\":\"x\"}}}}";
    Gson gson = new Gson();
    Schema schema = gson.fromJson(schemaJSON, Schema.class);

    assertEquals(schemaJSON, gson.toJson(schema));
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
}
