package io.jddf.gson;

/**
 * MaxDepthExceededException indicates that the maximum value of a Validator was
 * exceeded during validation.
 *
 * This can be raised from
 * {@link Validator#validate(Schema, com.google.gson.JsonElement)}.
 */
public class MaxDepthExceededException extends Exception {
  private static final long serialVersionUID = -5792237538755491171L;
}
