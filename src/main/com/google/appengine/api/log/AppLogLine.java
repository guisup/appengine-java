// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.log;

import com.google.appengine.api.log.LogService.LogLevel;

/**
 * An AppLogLine contains all the information for a single application-level
 * log. Specifically, this information is: (1) the time at which the logged
 * event occurred, (2) the level that the event was logged at, and (3) the
 * message associated with this event. AppLogLines may be inserted by the user
 * via logging frameworks, or by App Engine itself if we wish to alert the user
 * that certain events have occurred.
 *
 *
 */
public final class AppLogLine {
  private long timeUsec;
  private LogLevel logLevel;
  private String logMessage;

  /**
   * Default zero-argument constructor that creates an AppLogLine.
   */
  public AppLogLine() {

  }

  /**
   * Constructs a new application-level log.
   *
   * @param newTimeUsec The time that the logged event has occurred at, in
   *   microseconds since epoch.
   * @param newLogLevel The level that the event was logged at.
   * @param newLogMessage The message associated with this event.
   */
  AppLogLine(long newTimeUsec, LogLevel newLogLevel, String newLogMessage) {
    timeUsec = newTimeUsec;
    logLevel = newLogLevel;
    logMessage = newLogMessage;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }

  public String getLogMessage() {
    return logMessage;
  }

  public long getTimeUsec() {
    return timeUsec;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
  }

  public void setLogMessage(String logMessage) {
    this.logMessage = logMessage;
  }

  public void setTimeUsec(long timeUsec) {
    this.timeUsec = timeUsec;
  }
}