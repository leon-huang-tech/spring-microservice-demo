package com.demo.user.exception;

/**
 * Business Exception
 */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

  /**
   * Business exception to indicate that a requested resource was not found.
   */
  public ResourceNotFoundException(String message) {
        super(message);
    }
}