package io.jddf.gson;

/**
 * InvalidSchemaException is a base class for all the exceptions associated with
 * an invalid schema.
 * <p>
 *
 * Sub-classes of this class are returned from {@link Schema#verify()}.
 */
public class InvalidSchemaException extends Exception {
  private static final long serialVersionUID = 1L;
}
