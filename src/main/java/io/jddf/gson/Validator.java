package io.jddf.gson;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Validator {
  private int maxDepth;
  private int maxErrors;

  public List<ValidationError> validate(Schema schema, JsonElement instance) throws MaxDepthExceededException {
    VM vm = new VM(this.maxDepth, this.maxErrors, schema);

    try {
      vm.validate(schema, instance, null);
    } catch (TooManyErrorsException e) {
      // Ignore this error. It's just a circuit-breaker internal to VM.
    }

    return vm.errors;
  }

  private static class VM {
    private ArrayList<String> instanceTokens;
    private ArrayList<ArrayList<String>> schemaTokens;
    private ArrayList<ValidationError> errors;
    private int maxDepth;
    private int maxErrors;
    private Schema root;

    public VM(int maxDepth, int maxErrors, Schema root) {
      this.instanceTokens = new ArrayList<>();
      this.schemaTokens = new ArrayList<>();
      this.schemaTokens.add(new ArrayList<>());
      this.errors = new ArrayList<>();
      this.maxDepth = maxDepth;
      this.maxErrors = maxErrors;
      this.root = root;
    }

    public void validate(Schema schema, JsonElement instance, String parentTag)
        throws TooManyErrorsException, MaxDepthExceededException {
      switch (schema.getForm()) {
      case EMPTY:
        return;
      case REF:
        if (this.schemaTokens.size() == this.maxDepth) {
          throw new MaxDepthExceededException();
        }

        this.schemaTokens.add(new ArrayList<>(Arrays.asList("definitions", schema.getRef())));
        this.validate(this.root.getDefinitions().get(schema.getRef()), instance, null);

        this.schemaTokens.remove(this.schemaTokens.size() - 1);
        return;
      case TYPE:
        this.pushSchemaToken("type");
        switch (schema.getType()) {
        case BOOLEAN:
          if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isBoolean()) {
            this.pushError();
          }

          break;
        case FLOAT32:
        case FLOAT64:
          if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isNumber()) {
            this.pushError();
          }

          break;
        case INT8:
          this.checkInt(-128, 127, instance);
          break;
        case UINT8:
          this.checkInt(0, 255, instance);
          break;
        case INT16:
          this.checkInt(-32768, 32767, instance);
          break;
        case UINT16:
          this.checkInt(0, 65535, instance);
          break;
        case INT32:
          this.checkInt(-2147483648, 2147483647, instance);
          break;
        case UINT32:
          this.checkInt(0, 4294967295L, instance);
          break;
        case STRING:
          if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isString()) {
            this.pushError();
          }

          break;
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
          break;
        }

        this.popSchemaToken();
        return;
      case ENUM:
        this.pushSchemaToken("enum");
        if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isString()) {
          this.pushError();
        } else {
          if (!schema.getEnum().contains(instance.getAsString())) {
            this.pushError();
          }
        }
        this.popSchemaToken();
        return;
      case ELEMENTS:
        this.pushSchemaToken("elements");
        if (!instance.isJsonArray()) {
          this.pushError();
        } else {
          int index = 0;
          for (JsonElement subInstance : instance.getAsJsonArray()) {
            this.pushInstanceToken(Integer.toString(index));
            this.validate(schema.getElements(), subInstance, null);
            this.popInstanceToken();

            index += 1;
          }
        }
        this.popSchemaToken();
        return;
      case PROPERTIES:
        if (instance.isJsonObject()) {
          if (schema.getProperties() != null) {
            this.pushSchemaToken("properties");
            for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
              this.pushSchemaToken(entry.getKey());
              if (instance.getAsJsonObject().has(entry.getKey())) {
                this.pushInstanceToken(entry.getKey());
                this.validate(entry.getValue(), instance.getAsJsonObject().get(entry.getKey()), null);
                this.popInstanceToken();
              } else {
                this.pushError();
              }
              this.popSchemaToken();
            }
            this.popSchemaToken();
          }

          if (schema.getOptionalProperties() != null) {
            this.pushSchemaToken("optionalProperties");
            for (Map.Entry<String, Schema> entry : schema.getOptionalProperties().entrySet()) {
              this.pushSchemaToken(entry.getKey());
              if (instance.getAsJsonObject().has(entry.getKey())) {
                this.pushInstanceToken(entry.getKey());
                this.validate(entry.getValue(), instance.getAsJsonObject().get(entry.getKey()), null);
                this.popInstanceToken();
              }
              this.popSchemaToken();
            }
            this.popSchemaToken();
          }

          if (schema.getAdditionalProperties() == null || !schema.getAdditionalProperties()) {
            for (String key : instance.getAsJsonObject().keySet()) {
              boolean inProperties = schema.getProperties() != null && schema.getProperties().containsKey(key);
              boolean inOptionalProperties = schema.getOptionalProperties() != null
                  && schema.getOptionalProperties().containsKey(key);
              boolean discriminatorTagException = key.equals(parentTag);

              if (!inProperties && !inOptionalProperties && !discriminatorTagException) {
                this.pushInstanceToken(key);
                this.pushError();
                this.popInstanceToken();
              }
            }
          }
        } else {
          if (schema.getProperties() == null) {
            this.pushSchemaToken("optionalProperties");
          } else {
            this.pushSchemaToken("properties");
          }

          this.pushError();
          this.popSchemaToken();
        }

        return;
      case VALUES:
        this.pushSchemaToken("values");
        if (instance.isJsonObject()) {
          for (Map.Entry<String, JsonElement> entry : instance.getAsJsonObject().entrySet()) {
            this.pushInstanceToken(entry.getKey());
            this.validate(schema.getValues(), entry.getValue(), null);
            this.popInstanceToken();
          }
        } else {
          this.pushError();
        }
        this.popSchemaToken();
        return;
      case DISCRIMINATOR:
        this.pushSchemaToken("discriminator");
        if (instance.isJsonObject()) {
          JsonObject instanceObj = instance.getAsJsonObject();

          if (instanceObj.has(schema.getDiscriminator().getTag())) {
            JsonElement instanceTag = instanceObj.get(schema.getDiscriminator().getTag());
            if (instanceTag.isJsonPrimitive() && ((JsonPrimitive) instanceTag).isString()) {
              String instanceTagString = instanceTag.getAsString();
              if (schema.getDiscriminator().getMapping().containsKey(instanceTagString)) {
                Schema subSchema = schema.getDiscriminator().getMapping().get(instanceTagString);

                this.pushSchemaToken("mapping");
                this.pushSchemaToken(instanceTagString);
                this.validate(subSchema, instance, schema.getDiscriminator().getTag());
                this.popSchemaToken();
                this.popSchemaToken();
              } else {
                this.pushSchemaToken("mapping");
                this.pushInstanceToken(schema.getDiscriminator().getTag());
                this.pushError();
                this.popInstanceToken();
                this.popSchemaToken();
              }
            } else {
              this.pushSchemaToken("tag");
              this.pushInstanceToken(schema.getDiscriminator().getTag());
              this.pushError();
              this.popInstanceToken();
              this.popSchemaToken();
            }
          } else {
            this.pushSchemaToken("tag");
            this.pushError();
            this.popSchemaToken();
          }
        } else {
          this.pushError();
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

    private void pushInstanceToken(String token) {
      this.instanceTokens.add(token);
    }

    private void popInstanceToken() {
      this.instanceTokens.remove(this.instanceTokens.size() - 1);
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

      if (this.errors.size() == this.maxErrors) {
        throw new TooManyErrorsException();
      }
    }
  }

  private static class TooManyErrorsException extends Exception {
    private static final long serialVersionUID = 8424595334761933278L;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public int getMaxErrors() {
    return maxErrors;
  }

  public void setMaxErrors(int maxErrors) {
    this.maxErrors = maxErrors;
  }

  @Override
  public String toString() {
    return "Validator [maxDepth=" + maxDepth + ", maxErrors=" + maxErrors + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + maxDepth;
    result = prime * result + maxErrors;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Validator other = (Validator) obj;
    if (maxDepth != other.maxDepth)
      return false;
    if (maxErrors != other.maxErrors)
      return false;
    return true;
  }
}
