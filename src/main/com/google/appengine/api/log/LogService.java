// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.log;

/**
 * {@code LogService} allows callers to request the logs (and associated
 * information) using user-supplied filters. Logs are returned in a
 * {@link LogQueryResult} that can be iterated over to provide a series of
 * {@link RequestLogs}, which contain request-level logs as well as
 * application-level logs (described by {@link AppLogLine} objects).
 *
 */
public interface LogService {
  /**
   * The number of items that a single underlying fetch RPC call will retrieve,
   * if the user does not specify a batchSize.
   */
  int DEFAULT_ITEMS_PER_FETCH = 20;

  /**
   * The maximum number of items that a user can retrieve via a single
   * underlying fetch RPC call.
   */
  int MAX_ITEMS_PER_FETCH = 100;

  enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL,
  }

  /**
   * Retrieve logs for the current application with the constraints provided
   * by the user as parameters to this function. Acts synchronously.
   *
   * @param query A LogQuery object that contains the various query parameters
   * that should be used in the LogReadRequest.
   * @return An Iterable that contains a set of logs matching the
   * requested filters.
   */
  Iterable<RequestLogs> fetch(LogQuery query);
}
