// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.log;

/**
 * Creates {@link LogService} implementations.
 *
 */
public final class LogServiceFactory {

  /**
   * Creates a {@code LogService}.
   */
  public static LogService getLogService() {
    return new LogServiceImpl();
  }
}
