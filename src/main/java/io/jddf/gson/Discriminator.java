package io.jddf.gson;

import java.util.Map;

/**
 * Discriminator represents the {@code discriminator} part of a Schema.
 */
public class Discriminator {
  private String tag;
  private Map<String, Schema> mapping;

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Map<String, Schema> getMapping() {
    return mapping;
  }

  public void setMapping(Map<String, Schema> mapping) {
    this.mapping = mapping;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
    Discriminator other = (Discriminator) obj;
    if (mapping == null) {
      if (other.mapping != null)
        return false;
    } else if (!mapping.equals(other.mapping))
      return false;
    if (tag == null) {
      if (other.tag != null)
        return false;
    } else if (!tag.equals(other.tag))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Discriminator [mapping=" + mapping + ", tag=" + tag + "]";
  }
}
