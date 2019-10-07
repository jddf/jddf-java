package io.jddf.gson;

/**
 * RepeatedPropertyException indicates that a property was overdetermined in a
 * schema.
 * <p>
 *
 * This can arise from some combination of {@code properties},
 * {@code optionalProperties}, and {@code discriminator.tag} referring to the
 * same property. Doing so is incorrect in JDDF, as it makes it ambiguous what
 * the requirements on a property are.
 */
public class RepeatedPropertyException extends InvalidSchemaException {
  private static final long serialVersionUID = -8326866690426213354L;
  private String property;

  public RepeatedPropertyException(String property) {
    this.property = property;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((property == null) ? 0 : property.hashCode());
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
    RepeatedPropertyException other = (RepeatedPropertyException) obj;
    if (property == null) {
      if (other.property != null)
        return false;
    } else if (!property.equals(other.property))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RepeatedPropertyException [property=" + property + "]";
  }
}
