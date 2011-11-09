// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appengine.tools.development;

import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.BackendsXml;

import java.io.File;
import java.util.Map;

/**
 * Provides the backing servlet container support for the {@link DevAppServer},
 * as discovered via {@link ServiceProvider}.
 * <p>
 * More specifically, this interface encapsulates the interactions between the
 * {@link DevAppServer} and the underlying servlet container, which by default
 * uses Jetty.
 *
 */
public interface ContainerService {

  /**
   * The severity with which we'll treat environment variable mismatches.
   */
  enum EnvironmentVariableMismatchSeverity {
    WARNING,
    ERROR,
    IGNORE
  }

  /**
   * Sets up the necessary configuration parameters.
   *
   * @param devAppServerVersion Version of the devAppServer.
   * @param appDir The location of the application to run.
   * @param webXmlLocation The location of a file whose format complies with
   * http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd.  If null we will use
   * <appDir>/WEB-INF/web.xml.
   * @param appEngineWebXmlLocation The location of the app engine config file.
   * If null we will use <appDir>/WEB-INF/appengine-web.xml.
   * @param address The address on which the server will run
   * @param port The port to which the server will be bound.  If 0, an
   * available port will be selected.
   * @param containerConfigProperties Additional properties used in the
   * configuration of the specific container implementation.  This map travels
   * across classloader boundaries, so all values in the map must be JRE
   * classes.
   *
   * @return A LocalServerEnvironment describing the environment in which
   * the server is running.
   */
  LocalServerEnvironment configure(String devAppServerVersion, File appDir,
      File webXmlLocation, File appEngineWebXmlLocation,
      String address, int port, Map<String, Object> containerConfigProperties);

  /**
   * Starts up the servlet container.
   *
   * @throws Exception Any exception from the container will be rethrown as is.
   */
  void startup() throws Exception;

  /**
   * Shuts down the servlet container.
   *
   * @throws Exception Any exception from the container will be rethrown as is.
   */
  void shutdown() throws Exception;

  /**
   * Returns the listener network address, however it's decided during
   * the servlet container deployment.
   */
  String getAddress();

  /**
   * Returns the listener port number, however it's decided during the servlet
   * container deployment.
   */
  int getPort();

  /**
   * Returns the host name of the server, however it's decided during the
   * the servlet container deployment.
   */
  String getHostName();

  /**
   * Returns the context representing the currently executing webapp.
   */
  AppContext getAppContext();

  /**
   * Return the AppEngineWebXml configuration of this container
   */
  AppEngineWebXml getAppEngineWebXmlConfig();

  BackendsXml getBackendsXml();

  /**
   * Overrides the default EnvironmentVariableMismatchSeverity setting, to
   * disable exceptions during the testing.
   *
   * @param val The new EnvironmentVariableMismatchSeverity.
   * @see EnvironmentVariableMismatchSeverity
   */
  void setEnvironmentVariableMismatchSeverity(EnvironmentVariableMismatchSeverity val);

  /**
   * Get a set of properties to be passed to each service, based on the
   * AppEngineWebXml configuration.
   *
   * @return the map of properties to be passed to each service.
   */
  Map<String, String> getServiceProperties();
}
