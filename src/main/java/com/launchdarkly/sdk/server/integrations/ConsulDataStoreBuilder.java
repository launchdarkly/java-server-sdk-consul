package com.launchdarkly.sdk.server.integrations;

import com.google.common.net.HostAndPort;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.interfaces.ClientContext;
import com.launchdarkly.sdk.server.interfaces.DiagnosticDescription;
import com.launchdarkly.sdk.server.interfaces.PersistentDataStoreFactory;
import com.launchdarkly.sdk.server.interfaces.PersistentDataStore;
import com.launchdarkly.sdk.LDValue;
import com.orbitz.consul.Consul;

import java.net.URL;

/**
 * A builder for configuring the Consul-based persistent data store.
 * <p>
 * Obtain an instance of this class by calling {@link com.launchdarkly.sdk.server.integrations.Consul#dataStore()}. After calling its methods
 * to specify any desired custom settings, wrap it in a {@link com.launchdarkly.sdk.server.integrations.PersistentDataStoreBuilder}
 * by calling {@code Components.persistentDataStore()}, then pass the result into the SDK configuration with
 * {@link com.launchdarkly.sdk.server.LDConfig.Builder#dataStore(com.launchdarkly.sdk.server.interfaces.DataStoreFactory)}.
 * You do not need to call {@link #createPersistentDataStore(ClientContext)} yourself to build the actual data store; that
 * will be done by the SDK.
 * <p>
 * The Consul client has many configuration options. This class has corresponding methods for
 * some of the most commonly used ones. If you need more sophisticated control over the
 * Consul client, you can construct one of your own and pass it in with the
 * {@link #existingClient(Consul)} method.
 * <p>
 * Builder calls can be chained, for example:
 *
 * <pre><code>
 *     LDConfig config = new LDConfig.Builder()
 *         .dataStore(
 *             Components.persistentDataStore(
 *                 Consul.dataStore()
 *                     .host("my-consul-host")
 *                     .prefix("app1")
 *             ).cacheSeconds(15)
 *         )
 *         .build();
 * </code></pre>
 * 
 * @since 1.1.0
 */
public final class ConsulDataStoreBuilder implements PersistentDataStoreFactory, DiagnosticDescription {
  /**
   * The default value for {@link #prefix(String)}.
   */
  public static final String DEFAULT_PREFIX = "launchdarkly";
  
  private String prefix = DEFAULT_PREFIX;
  private HostAndPort hostAndPort;
  private URL url;
  private Consul existingClient;
  
  ConsulDataStoreBuilder() {
  }

  private Consul createClient() {
    if (existingClient != null) {
      return existingClient;
    }
    Consul.Builder builder = Consul.builder();
    if (hostAndPort != null) {
      builder = builder.withHostAndPort(hostAndPort);
    }
    if (url != null) {
      builder = builder.withUrl(url);
    }
    return builder.build();
  }
  
  /**
   * Specifies the hostname of the Consul agent. By default, it is {@code localhost}.
   *
   * @param host the hostname of the Consul agent
   * @return the builder
   * @see #port(int)
   */
  public ConsulDataStoreBuilder host(String host) {
    if (host == null) {
      hostAndPort = null;
    } else {
      if (hostAndPort == null) {
        hostAndPort = HostAndPort.fromParts(host, Consul.DEFAULT_HTTP_PORT);
      } else {
        hostAndPort = HostAndPort.fromParts(host, hostAndPort.getPort());
      }
    }
    url = null;
    return this;
  }

  /**
   * Specifies the port that the Consul agent is running on. By default, it is 8500.
   *
   * @param port the port of the Consul agent
   * @return the builder
   * @see #host(String)
   */
  public ConsulDataStoreBuilder port(int port) {
    if (hostAndPort == null) {
      hostAndPort = HostAndPort.fromParts(Consul.DEFAULT_HTTP_HOST, port);
    } else {
      hostAndPort = HostAndPort.fromParts(hostAndPort.getHost(), port);
    }
    url = null;
    return this;
  }

  /**
   * Specifies the Consul agent's location as a URL.
   * 
   * @param url the Consul agent URL
   * @return the builder
   */
  public ConsulDataStoreBuilder url(URL url) {
    this.url = url;
    this.hostAndPort = null;
    return this;
  }
  
  /**
   * Specifies an existing Consul client instance for the feature store to use. If you do this,
   * other Consul configuration options from this builder such as {@link #host(String)} will be
   * ignored.
   * 
   * @param client a Consul client instance
   * @return the builder
   */
  public ConsulDataStoreBuilder existingClient(Consul client) {
    this.existingClient = client;
    return this;
  }
  
  /**
   * Sets the namespace prefix for all keys stored in Consul. Use this if you are sharing
   * the same database table between multiple clients that are for different LaunchDarkly
   * environments, to avoid key collisions. 
   *
   * @param prefix the namespace prefix
   * @return the builder
   */
  public ConsulDataStoreBuilder prefix(String prefix) {
    this.prefix = (prefix == null || prefix.equals("")) ? DEFAULT_PREFIX : prefix;
    return this;
  }

  /**
   * Called internally by the SDK to create the actual data store instance.
   * @return the data store configured by this builder
   */
  @Override
  public PersistentDataStore createPersistentDataStore(ClientContext context) {
    return new ConsulDataStoreImpl(createClient(), prefix);
  }

  @Override
  public LDValue describeConfiguration(LDConfig config) {
    return LDValue.of("Consul");
  }
}
