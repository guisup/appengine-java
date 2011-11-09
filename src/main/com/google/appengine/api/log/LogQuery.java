// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api.log;

import com.google.appengine.api.log.LogService.LogLevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object that contains the various query parameters that the user wishes
 * to use for their call to {@link LogService#fetch(LogQuery)}. Users are
 * expected to use the {@link LogQuery.Builder} class provided here to construct a default
 * query and then use the with* methods to modify it as they need to when
 * building their query.
 *
 */
public final class LogQuery implements Serializable, Cloneable {
  private String offset;

  private Long startTimeUsec;

  private Long endTimeUsec;

  private int batchSize = LogService.DEFAULT_ITEMS_PER_FETCH;

  private LogLevel minLogLevel;
  private boolean includeIncomplete = false;
  private boolean includeAppLogs = false;
  private List<String> majorVersionIds = new ArrayList<String>();

  private static final String MAJOR_VERSION_ID_REGEX = "[a-z\\d][a-z\\d\\-]{0,99}";
  private static final Pattern VERSION_PATTERN = Pattern.compile(MAJOR_VERSION_ID_REGEX);

  /**
   * An object that builds LogQuery objects based on the possible query
   * parameters for calls to {@link LogService#fetch(LogQuery)}, or its
   * asynchronous counterpart {@link LogService#fetchAsync(LogQuery)}. Users
   * should typically get the default LogQuery via
   * {@link LogQuery.Builder#withDefaults()} and modify it as needed via the
   * setters provided, or if only a single modification is needed, use the
   * convenience methods provided (e.g.,
   * {@link LogQuery.Builder#withStartTimeUsec(long)}.
   *
   *
   */
  public static final class Builder {
    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: the offset into the database representing
     * where the next read should begin at. This method is for internal use
     * only.
     *
     * @param offset The String representation of where the next result should
     *   be read from.
     * @return A LogQuery that internally sets its offset to the user-provided
     *   one.
     */
    static LogQuery withOffset(String offset) {
      return withDefaults().offset(offset);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: the earliest time (in microseconds since
     * epoch) that returned requests should possess. By default, this value is
     * not set, which indicates a starting time at the epoch should be used
     * when acquiring logs.
     *
     * @param startTimeUsec The earliest time (inclusive) for returned requests.
     * @return A LogQuery that internally sets its starting time to the
     *   user-provided one.
     */
    public static LogQuery withStartTimeUsec(long startTimeUsec) {
      return withDefaults().startTimeUsec(startTimeUsec);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: the latest time (in microseconds since epoch)
     * that returned requests should possess. The latest time specified here is
     * therefore checked against the starting time of results queried, since we
     * are specifically interested in capturing the latest starting time for
     * that request. By default, this value is not set, which indicates that the
     * ending time should be set to the time when the fetch() call is made.
     *
     * @param endTimeUsec The latest time (exclusive) for returned requests.
     * @return A LogQuery that internally sets its ending time to the
     *   user-provided one.
     */
    public static LogQuery withEndTimeUsec(long endTimeUsec) {
      return withDefaults().endTimeUsec(endTimeUsec);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: the maximum number of items that a single
     * underlying fetch RPC should return. By default, this value is set to
     * {@link LogService#DEFAULT_ITEMS_PER_FETCH}: users can set it lower
     * than this if they like but cannot raise it beyond this limit.
     *
     * @param batchSize The maximum number of items that a single fetch request
     *   should return.
     * @return A LogQuery that internally sets its batch size to the
     *   user-provided one.
     */
    public static LogQuery withBatchSize(int batchSize) {
      return withDefaults().batchSize(batchSize);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: the minimum application log level. This means
     * that any request log returned has at least one application log meeting or
     * exceeding the minimum log level given (although it will contain all
     * application logs for that request).
     *
     * @param minLogLevel The minimum application log level to search for in
     *   request logs.
     * @return A LogQuery that internally sets the minimum log level to use to
     *   be the user-provided one.
     */
    public static LogQuery withMinLogLevel(LogLevel minLogLevel) {
      return withDefaults().minLogLevel(minLogLevel);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: whether or not incomplete requests should be
     * included in the results. Incomplete requests are requests that are still
     * in flight: that is, they have started to be processed but have not yet
     * finished. By default, incomplete requests are excluded from queries.
     *
     * @param includeIncomplete Whether or not incomplete requests should be
     *   returned.
     * @return A LogQuery that internally sets its includeIncomplete parameter
     *   to be the user-provided one.
     */
    public static LogQuery withIncludeIncomplete(boolean includeIncomplete) {
      return withDefaults().includeIncomplete(includeIncomplete);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: whether or not application-level logs should
     * be returned in the results. Note that request-level logs will always be
     * returned, regardless of the value set here. By default, application-level
     * logs are not included in queries.
     *
     * @param includeAppLogs Whether or not application-level logs should be
     *   included in the results.
     * @return A LogQuery that internally sets its includeAppLogs parameter to
     *   be the user-provided one.
     */
    public static LogQuery withIncludeAppLogs(boolean includeAppLogs) {
      return withDefaults().includeAppLogs(includeAppLogs);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only a single
     * non-default parameter set: the major version IDs whose logs should be
     * queried over. By default, the major version servicing the current request
     * is set as the only version ID whose logs should be queried over.
     *
     * @param versionIds The list of major version IDs whose logs should be
     *   queried over.
     * @return A LogQuery that internally sets the set of major version IDs to
     *   query over to be the user-provided one.
     */
    public static LogQuery withMajorVersionIds(List<String> versionIds) {
      return withDefaults().majorVersionIds(versionIds);
    }

    /**
     * Provides callers with a way to get a LogQuery object with only the
     * default parameters set. The value of each default parameter is explained
     * in that section's Builder method (e.g., the default value for versionIds
     * is discussed in {@link LogQuery.Builder#withMajorVersionIds(List)}.
     *
     * @return A LogQuery with only the default parameters set.
     */
    public static LogQuery withDefaults() {
      return new LogQuery();
    }
  }

  /**
   * Makes a copy of a provided LogQuery. We only copy over set fields, and skip
   * over non-set (null) fields.
   *
   * @return A new LogQuery whose fields are copied from the given LogQuery.
   */
  @Override
  protected LogQuery clone() {
    LogQuery clone;
    try {
      clone = (LogQuery) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }

    clone.majorVersionIds = new ArrayList<String>(majorVersionIds);
    return clone;
  }

  /**
   * An internal method used by {@link LogQuery.Builder#withOffset(String)} to
   * provide a setter that accommodates offsets provided by calls to
   * {@link LogService#fetch(LogQuery)} and
   * {@link LogService#fetchAsync(LogQuery)}.
   *
   * @param offset The next location that logs can be found at.
   * @return This object, with its offset set to the user-provided one. This
   *   object is returned instead of a new one so that it this call can be
   *   chained with the other setters.
   */
  LogQuery offset(String offset) {
    this.offset = offset;
    return this;
  }

  /**
   * A setter that allows for user-specified query starting times.
   *
   * @param startTimeUsec The earliest time for returned results (inclusive), in
   *   microseconds since the Unix epoch.
   * @return This object, with its starting time set to the user-provided one.
   *   This object is returned instead of a new one so that it this call can be
   *   chained with the other setters.
   */
  public LogQuery startTimeUsec(long startTimeUsec) {
    this.startTimeUsec = startTimeUsec;
    return this;
  }

  /**
   * A setter that allows for user-specified query ending times.
   *
   * @param endTimeUsec The latest time for returned results (exclusive), in
   * microseconds since the Unix epoch.
   * @return This object, with its ending time set to the user-provided one.
   *   This object is returned instead of a new one so that it this call can be
   *   chained with the other setters.
   */
  public LogQuery endTimeUsec(long endTimeUsec) {
    this.endTimeUsec = endTimeUsec;
    return this;
  }

  /**
   * A setter that allows for user-specified batch sizes. The value set
   * cannot be less than one, and cannot be greater than
   * {@link LogService#MAX_ITEMS_PER_FETCH}.
   *
   * @param batchSize The maximum number of results that each call to
   *   {@link LogService#fetch(LogQuery)} or
   *   {@link LogService#fetchAsync(LogQuery)} can return.
   * @return This object, with its batch size set to the user-provided one.
   *   This object is returned instead of a new one so that it this call can be
   *   chained with the other setters.
   */
  public LogQuery batchSize(int batchSize) {
    if (batchSize < 1) {
      throw new IllegalArgumentException("batchSize must be greater than zero");
    }

    if (batchSize > LogService.MAX_ITEMS_PER_FETCH) {
      throw new IllegalArgumentException("batchSize specified was too large");
    }

    this.batchSize = batchSize;
    return this;
  }

  /**
   * A setter that allows the user to specify the minimum logging level
   * desired when searching the application's request logs.
   *
   * @param minLogLevel The minimum application log level to search for in the
   *   application's request logs.
   * @return This object, with its minLogLevel set to the user-provided one.
   *   This object is returned instead of a new one so that it this call can be
   *   chained with the other setters.
   */
  public LogQuery minLogLevel(LogLevel minLogLevel) {
    this.minLogLevel = minLogLevel;
    return this;
  }

  /**
   * A setter that allows the user to specify whether or not they want
   * incomplete request data returned in their results.
   *
   * @param includeIncomplete Whether or not incomplete request log info should
   *   be included in the resulting log data.
   * @return This object, with its includeIncomplete set to the user-provided
   *   one. This object is returned instead of a new one so that it this call
   *   can be chained with the other setters.
   */
  public LogQuery includeIncomplete(boolean includeIncomplete) {
    this.includeIncomplete = includeIncomplete;
    return this;
  }

  /**
   * A setter that allows the user to specify whether or not they want
   * application-level logs in their results.
   *
   * @param includeAppLogs Whether or not application-level logs should be
   *   returned in the resulting log data.
   * @return This object, with its includeAppLogs set to the user-provided
   *   one. This object is returned instead of a new one so that it this call
   *   can be chained with the other setters.
   */
  public LogQuery includeAppLogs(boolean includeAppLogs) {
    this.includeAppLogs = includeAppLogs;
    return this;
  }

  /**
   * A setter that allows the user to specify the major app versions whose
   * log data should be read.
   *
   * @param versionIds The major app versions whose log data should be read.
   * @return This object, with its versionIds set to the user-provided
   *   one. This object is returned instead of a new one so that it this call
   *   can be chained with the other setters.
   */
  public LogQuery majorVersionIds(List<String> versionIds) {
    for (String versionId : versionIds) {
      Matcher matcher = VERSION_PATTERN.matcher(versionId);
      if (!matcher.matches()) {
        throw new IllegalArgumentException("versionIds must only contain valid " +
          "major version identifiers. Version " + versionId + " is not a valid " +
          "major version identifier.");
      }
    }

    this.majorVersionIds = versionIds;
    return this;
  }

  /**
   * @return The next location where logs can be read from. This will be null if
   *   no more logs are available for the given query parameters.
   */
  String getOffset() {
    return offset;
  }

  /**
   * @return The maximum number of results that a single call to
   *   {@link LogService#fetch(LogQuery)} can return.
   */
  public Integer getBatchSize() {
    return batchSize;
  }

  /**
   * @return The latest time that request data queried for should possess
   *   (exclusive), in microseconds since the Unix epoch. This will be null if
   *   it has not been previously set.
   */
  public Long getEndTimeUsec() {
    return endTimeUsec;
  }

  /**
   * @return Whether or not application-level logs should be returned.
   */
  public Boolean getIncludeAppLogs() {
    return includeAppLogs;
  }

  /**
   * @return Whether or not incomplete request logs should be returned.
   */
  public Boolean getIncludeIncomplete() {
    return includeIncomplete;
  }

  /**
   * @return The minimum log level to search for in this app's request logs.
   */
  public LogLevel getMinLogLevel() {
    return minLogLevel;
  }

  /**
   * @return The earliest time that request data queried for should possess
   *   (inclusive), in microseconds since the Unix epoch. This will be null if
   *   it has not been previously set.
   */
  public Long getStartTimeUsec() {
    return startTimeUsec;
  }

  /**
   * @return The list of major app versions that should be queried over.
   */
  public List<String> getMajorVersionIds() {
    return majorVersionIds;
  }
}
