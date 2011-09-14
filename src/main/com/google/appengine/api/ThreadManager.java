// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.api;

import com.google.apphosting.api.ApiProxy;
import java.util.concurrent.ThreadFactory;

/**
 * {@code ThreadManager} exposes a {@link ThreadFactory} that allows
 * App Engine applications to spawn new threads.
 *
 *
 */
public final class ThreadManager {
  private static final String REQUEST_THREAD_FACTORY_ATTR =
      "com.google.appengine.api.ThreadManager.REQUEST_THREAD_FACTORY";

  /**
   * Returns a {@link ThreadFactory} that will create threads scoped
   * to the current request.  These threads will be interrupted at the
   * end of the current request and must complete within the request
   * deadline.
   *
   * <p>Your code has limited access to the threads created by this
   * {@link ThreadFactory}.  For example, you can call
   * {@link Thread#setUncaughtExceptionHandler} and
   * {@link Thread#interrupt}, but not {@link Thread#stop} or any
   * other methods that require
   * {@code RuntimePermission("modifyThread")}.
   */
  public static ThreadFactory currentRequestThreadFactory() {
    return (ThreadFactory) ApiProxy.getCurrentEnvironment().getAttributes().get(
        REQUEST_THREAD_FACTORY_ATTR);
  }

  /**
   * Create a new Thread that executs {@code runnable}.  Calling this
   * method is equivalent to invoking {@link ThreadFactory#run} on the
   * ThreadFactory returned from {#link #currentRequestThreadFactory}.
   * This thread will be interrupted at the end of the current request
   * and must complete within the request deadline.
   */
  public static Thread createThreadForCurrentRequest(Runnable runnable) {
    return currentRequestThreadFactory().newThread(runnable);
  }

}
