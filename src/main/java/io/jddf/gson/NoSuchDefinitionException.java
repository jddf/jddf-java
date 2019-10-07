package io.jddf.gson;

/**
 * NoSuchDefinitionException indicates a reference to a definition which doesn't
 * exist.
 * <p>
 *
 * The "name" of NoSuchDefinitionException indicates the name of the referred-to
 * definition which doesn't exist.
 */
public class NoSuchDefinitionException extends InvalidSchemaException {
  private static final long serialVersionUID = -3765988719558355706L;
  private String name;

  public NoSuchDefinitionException(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    NoSuchDefinitionException other = (NoSuchDefinitionException) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "NoSuchDefinitionException [name=" + name + "]";
  }
}
