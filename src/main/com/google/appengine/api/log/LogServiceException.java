// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.log;

/**
 * Customized exception for all log service errors.
 *
 *
 */
public final class LogServiceException extends RuntimeException {

  /**
   * Constructs a new LogServiceException without an error message.
   */
  public LogServiceException() {
    this("");
  }

  /**
   * Constructs a new LogServiceException with an error message.
   * @param errorDetail Log service error detail.
   */
  LogServiceException(String errorDetail) {
    super("LogError : " + errorDetail);
  }
}
