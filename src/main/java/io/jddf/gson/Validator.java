package io.jddf.gson;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class Validator {
  public List<ValidationError> validate(Schema schema, JsonElement instance) {
    VM vm = new VM(0, 0, schema);

    try {
      vm.validate(schema, instance);
    } catch (TooManyErrorsException e) {
      // Ignore this error. It's just a circuit-breaker internal to VM.
    }

    return vm.errors;
  }

  private static class VM {
    private ArrayList<String> instanceTokens;
    private ArrayList<ArrayList<String>> schemaTokens;
    private ArrayList<ValidationError> errors;
    private int maxErrors;
    private int maxDepth;
    private Schema root;

    public VM(int maxErrors, int maxDepth, Schema root) {
      this.instanceTokens = new ArrayList<>();
      this.schemaTokens = new ArrayList<>();
      this.schemaTokens.add(new ArrayList<>());
      this.errors = new ArrayList<>();
      this.maxErrors = maxErrors;
      this.maxDepth = maxDepth;
      this.root = root;
    }

    public void validate(Schema schema, JsonElement instance) throws TooManyErrorsException {
      switch (schema.getForm()) {
      case TYPE:
        this.pushSchemaToken("type");
        switch (schema.getType()) {
        case BOOLEAN:
          if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isBoolean()) {
            this.pushError();
          }

          return;
        case FLOAT32:
        case FLOAT64:
          if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isNumber()) {
            this.pushError();
          }

          return;
        case INT8:
          this.checkInt(-128, 127, instance);
          return;
        case UINT8:
          this.checkInt(0, 255, instance);
          return;
        case INT16:
          this.checkInt(-32768, 32767, instance);
          return;
        case UINT16:
          this.checkInt(0, 65535, instance);
          return;
        case INT32:
          this.checkInt(-2147483648, 2147483647, instance);
          return;
        case UINT32:
          this.checkInt(0, 4294967295L, instance);
          return;
        case STRING:
          if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isString()) {
            this.pushError();
          }

          return;
        case TIMESTAMP:
          if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isString()) {
            this.pushError();
          } else {
            // The value *is* a JSON string. Let's verify it's a well-formatted
            // RFC3339 timestamp.
            try {
              DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(instance.getAsString());
            } catch (DateTimeParseException e) {
              this.pushError();
            }
          }
        }

        this.popSchemaToken();
        return;
      }
    }

    private void checkInt(long min, long max, JsonElement instance) throws TooManyErrorsException {
      if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isNumber()) {
        this.pushError();
      } else {
        double val = instance.getAsDouble();
        if (val < min || val > max || val != Math.round(val)) {
          this.pushError();
        }
      }
    }

    private void pushSchemaToken(String token) {
      this.schemaTokens.get(this.schemaTokens.size() - 1).add(token);
    }

    private void popSchemaToken() {
      ArrayList<String> tokens = this.schemaTokens.get(this.schemaTokens.size() - 1);
      tokens.remove(tokens.size() - 1);
    }

    private void pushError() throws TooManyErrorsException {
      ValidationError validationError = new ValidationError();
      validationError.setInstancePath(new ArrayList<>(this.instanceTokens));
      validationError.setSchemaPath(new ArrayList<>(this.schemaTokens.get(this.schemaTokens.size() - 1)));
      this.errors.add(validationError);
    }
  }

  private static class TooManyErrorsException extends Exception {
    private static final long serialVersionUID = 8424595334761933278L;
  }
}
