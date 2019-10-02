package io.jddf.gson;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import java.util.Set;

public class Schema {
  private String ref;
  private Type type;
  @SerializedName("enum")
  private Set<String> enm;
  private Schema elements;
  private Map<String, Schema> properties;
  private Map<String, Schema> optionalProperties;
  private Boolean additionalProperties;
  private Schema values;
  private Discriminator discriminator;

  public Form getForm() {
    if (this.getRef() != null) {
      return Form.REF;
    } else if (this.getType() != null) {
      return Form.TYPE;
    } else if (this.getEnum() != null) {
      return Form.ENUM;
    } else if (this.getElements() != null) {
      return Form.ELEMENTS;
    } else if (this.getProperties() != null) {
      return Form.PROPERTIES;
    } else if (this.getOptionalProperties() != null) {
      return Form.PROPERTIES;
    } else if (this.getValues() != null) {
      return Form.VALUES;
    } else if (this.getDiscriminator() != null) {
      return Form.DISCRIMINATOR;
    } else {
      return Form.EMPTY;
    }
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Set<String> getEnum() {
    return enm;
  }

  public void setEnum(Set<String> enm) {
    this.enm = enm;
  }

  public Schema getElements() {
    return elements;
  }

  public void setElements(Schema elements) {
    this.elements = elements;
  }

  public Map<String, Schema> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Schema> properties) {
    this.properties = properties;
  }

  public Map<String, Schema> getOptionalProperties() {
    return optionalProperties;
  }

  public void setOptionalProperties(Map<String, Schema> optionalProperties) {
    this.optionalProperties = optionalProperties;
  }

  public Boolean getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Boolean additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  public Schema getValues() {
    return values;
  }

  public void setValues(Schema values) {
    this.values = values;
  }

  public Discriminator getDiscriminator() {
    return discriminator;
  }

  public void setDiscriminator(Discriminator discriminator) {
    this.discriminator = discriminator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((additionalProperties == null) ? 0 : additionalProperties.hashCode());
    result = prime * result + ((discriminator == null) ? 0 : discriminator.hashCode());
    result = prime * result + ((elements == null) ? 0 : elements.hashCode());
    result = prime * result + ((enm == null) ? 0 : enm.hashCode());
    result = prime * result + ((optionalProperties == null) ? 0 : optionalProperties.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + ((ref == null) ? 0 : ref.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((values == null) ? 0 : values.hashCode());
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
    Schema other = (Schema) obj;
    if (additionalProperties == null) {
      if (other.additionalProperties != null)
        return false;
    } else if (!additionalProperties.equals(other.additionalProperties))
      return false;
    if (discriminator == null) {
      if (other.discriminator != null)
        return false;
    } else if (!discriminator.equals(other.discriminator))
      return false;
    if (elements == null) {
      if (other.elements != null)
        return false;
    } else if (!elements.equals(other.elements))
      return false;
    if (enm == null) {
      if (other.enm != null)
        return false;
    } else if (!enm.equals(other.enm))
      return false;
    if (optionalProperties == null) {
      if (other.optionalProperties != null)
        return false;
    } else if (!optionalProperties.equals(other.optionalProperties))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    if (ref == null) {
      if (other.ref != null)
        return false;
    } else if (!ref.equals(other.ref))
      return false;
    if (type != other.type)
      return false;
    if (values == null) {
      if (other.values != null)
        return false;
    } else if (!values.equals(other.values))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Schema [additionalProperties=" + additionalProperties + ", discriminator=" + discriminator + ", elements="
        + elements + ", enm=" + enm + ", optionalProperties=" + optionalProperties + ", properties=" + properties
        + ", ref=" + ref + ", type=" + type + ", values=" + values + "]";
  }
}
