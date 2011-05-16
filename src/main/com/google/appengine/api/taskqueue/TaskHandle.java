// Copyright 2010 Google Inc. All rights reserved.
package com.google.appengine.api.taskqueue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created from {@link Queue#add(TaskOptions)}.  Contains the
 * task name (generated if otherwise unspecified), task ETA (computed if
 * not specified) and queue name.  The queue name and task name
 * uniquely identify the task for an application.
 *
 */
public final class TaskHandle implements Serializable {
  private static final long serialVersionUID = -2578988193753847512L;
  private String taskName;
  private String queueName;
  private long etaMillis;
  private Integer retryCount;
  private TaskOptions options;

  TaskHandle(TaskOptions options, String queueName,Integer retryCount) {
    validateTaskName(options.getTaskName());
    QueueApiHelper.validateQueueName(queueName);
    this.queueName = queueName;
    this.retryCount = retryCount;
    this.taskName = null;
    this.etaMillis = 0;
    this.options = options;
  }

  TaskHandle(TaskOptions options, String queueName) {
    this(options, queueName, 0);
  }

  public TaskHandle(String name, String queueName, long etaMillis) {
    this(TaskOptions.Builder.withTaskName(name).etaMillis(etaMillis),
         queueName, null);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    if (this.options == null) {
      this.options = TaskOptions.Builder.withTaskName(taskName).etaMillis(etaMillis);
      this.taskName = null;
      this.etaMillis = 0;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = options.hashCode();
    result = result * prime + queueName.hashCode();
    result = result * prime + (retryCount == null ? 0 : retryCount.intValue());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TaskHandle other = (TaskHandle) obj;
    if (options == null) {
      if (other.options != null) return false;
    } else if (!options.equals(other.options)) return false;
    if (queueName == null) {
      if (other.queueName != null) return false;
    } else if (!queueName.equals(other.queueName)) return false;
    if (retryCount == null) {
      if (other.retryCount != null) return false;
    } else if (!retryCount.equals(other.retryCount)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "TaskHandle[options=" + options.toString() + "queueName=" + queueName
           + ", retryCount=" + retryCount + "]";
  }

  /**
   * Checks the name of this task matches QueueConstants.TASK_NAME_PATTERN
   *
   * @throws IllegalArgumentException
   */
  static void validateTaskName(String taskName) {
    if (taskName == null || taskName.length() == 0 ||
        !QueueConstants.TASK_NAME_PATTERN.matcher(taskName).matches()) {
      throw new IllegalArgumentException(
          "Task name does not match expression " + QueueConstants.TASK_NAME_REGEX +
          "; given taskname: '" + taskName + "'");
    }
  }

  /**
   * Returns the name of this task.  This may have been generated
   * by a call to {@link Queue#add()} if the name was not otherwise specified.
   */
  public String getName() {
    return options.getTaskName();
  }

  /**
   * Returns the name of the queue that this task was submitted into.
   */
  public String getQueueName() {
    return queueName;
  }

  /**
   * Returns a time comparable to {@link System#currentTimeMillis()} when
   * this task is scheduled for execution.
   */
  public long getEtaMillis() {
    return options.getEtaMillis();
  }

  /**
   * Returns number of leases that had been performed on this task.
   * Can return {@code null}.
   */
  public Integer getRetryCount() {
    return retryCount;
  }

  /**
   * Returns binary payload data of this task.
   * Can return {@code null}.
   */
  public byte[] getPayload() {
    return options.getPayload();
  }

  static final class KeyValuePair implements Map.Entry<String, String> {
    private final String key;
    private String value;

    public KeyValuePair(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String setValue(String v) {
      String old = value;
      value = v;
      return old;
    }

    public int hashCode() {
      int prime = 31;
      int result = 0;
      result = prime * result + key.hashCode();
      result = prime * result + value.hashCode();
      return result;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || !(o instanceof KeyValuePair)) return false;
      KeyValuePair that = (KeyValuePair)o;
      return key.equals(that.key) && value.equals(that.value);
    }
  }

  /**
   * Attempts to decode the {@code payload} byte array in our {@code options}
   * into a list of Map.Entry<String, String>.
   *
   * @throws UnsupportedEncodingException if the payload cannot be decoded as a
   * {@code application/x-www-form-urlencoded} string.
   * @throws UnsupportedOperationException if the {@code options} has no payload
   * or the payload bytes could not be interpreted as application/x-www-form-urlencoded
   * key-value pairs.
   */
  public List<Map.Entry<String, String>> extractParams()
      throws UnsupportedEncodingException, UnsupportedOperationException {
    String payload = new String(getPayload());
    String[] paramStrings = payload.split("&");

    List<Map.Entry<String, String>> result = new ArrayList<Map.Entry<String, String>>();
    for (String param : paramStrings) {
      String[] kv = param.split("=", 2);
      if (kv.length != 2) {
        throw new UnsupportedOperationException(
            "Payload " + payload + " failed to decode as application/x-www-form-urlencoded pairs. "
            + param + " Length" + kv.length);
      }
      result.add(new KeyValuePair(URLDecoder.decode(kv[0], "UTF-8"),
                                  URLDecoder.decode(kv[1], "UTF-8")));
    }
    return result;
  }
}
