// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.log;

import com.google.apphosting.api.logservice.LogServicePb.LogReadResponse;
import com.google.apphosting.api.logservice.LogServicePb.RequestLog;
import com.google.common.collect.AbstractIterator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An object that is the result of performing a LogService.fetch() operation.
 * LogQueryResults contain the logs from the user's query. Users of this service
 * should use the {@link LogQueryResult#iterator} provided by this class to
 * retrieve their results.
 *
 */
public final class LogQueryResult implements Serializable, Iterable<RequestLogs> {
  private final List<RequestLogs> logs;
  private final String cursor;
  private final LogQuery query;

  protected LogQueryResult(LogReadResponse response, LogQuery originalQuery) {
    logs = new ArrayList<RequestLogs>();
    for (RequestLog log : response.logs()) {
      logs.add(new RequestLogs(log));
    }

    cursor = response.getOffset().getRequestId();

    query = originalQuery.clone();
  }

  /**
   * Returns the list of logs internally kept by this class. The main user of
   * this method is iterator, who needs it to give the user logs as needed.
   *
   * @return A List of RequestLogs acquired from a fetch() request.
   */
  private List<RequestLogs> getLogs() {
    return Collections.unmodifiableList(logs);
  }

  /**
   * Returns the String version of the database cursor, which can be used to
   * tell subsequent fetch() requests where to start scanning from. The main
   * user of this method is iterator, who uses it to ensure that users get all
   * the logs they requested.
   *
   * @return A String representing the next location in the database to read
   *   from.
   */
  private String getCursor() {
    return cursor;
  }

  /**
   * Returns an Iterator that will yield all of the logs the user has requested.
   * If the user has asked for more logs than a single request can accommodate
   * (which is LogService.MAX_ITEMS_PER_FETCH), then this iterator grabs
   * the first batch and returns them until they are exhausted. Once they are
   * exhausted, a fetch() call is made to get more logs and the process is
   * repeated until either all of the logs have been read or the user has
   * stopped asking for more logs.
   *
   * @return An iterator that provides RequestLogs to the caller.
   */
  @Override
  public Iterator<RequestLogs> iterator() {
    return new AbstractIterator<RequestLogs>() {
      List<RequestLogs> iterLogs = logs;
      String iterCursor = cursor;
      int index = 0;
      int lengthLogs = iterLogs.size();

      @Override
      protected RequestLogs computeNext() {
        if (index >= lengthLogs) {
          if (iterCursor.length() == 0) {
            return endOfData();
          }

          query.offset(iterCursor);

          LogQueryResult nextResults = new LogServiceImpl().fetch(query);
          iterLogs = nextResults.getLogs();
          iterCursor = nextResults.getCursor();
          lengthLogs = iterLogs.size();
          index = 0;

          if (lengthLogs == 0) {
            return endOfData();
          }
        }

        return iterLogs.get(index++);
      }
    };
  }
}
