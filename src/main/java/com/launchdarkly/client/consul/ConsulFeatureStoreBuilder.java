package com.launchdarkly.client.consul;

import com.google.common.net.HostAndPort;
import com.launchdarkly.client.FeatureStore;
import com.launchdarkly.client.FeatureStoreCacheConfig;
import com.launchdarkly.client.FeatureStoreFactory;
import com.launchdarkly.client.utils.CachingStoreWrapper;
import com.orbitz.consul.Consul;

import java.net.URL;

/**
 * Builder/factory class for the Consul feature store.
 * <p>
 * Create this builder by calling {@link ConsulComponents#consulFeatureStore()}, then
 * optionally modify its properties with builder methods, and then include it in your client
 * configuration with {@link com.launchdarkly.client.LDConfig.Builder#featureStoreFactory(FeatureStoreFactory)}.
 * <p>
 * The Consul client has many configuration options. This class has corresponding methods for
 * some of the most commonly used ones. If you need more sophisticated control over the
 * Consul client, you can construct one of your own and pass it in with the
 * {@link #existingClient(Consul)} method.
 */
public class ConsulFeatureStoreBuilder implements FeatureStoreFactory {
  private String prefix = "launchdarkly";
  private HostAndPort hostAndPort;
  private URL url;
  private Consul existingClient;
  private FeatureStoreCacheConfig caching = FeatureStoreCacheConfig.DEFAULT;
  
  ConsulFeatureStoreBuilder() {
  }
  
  @Override
  public FeatureStore createFeatureStore() {
    Consul client = createClient();
    ConsulFeatureStoreCore core = new ConsulFeatureStoreCore(client, prefix);
    CachingStoreWrapper wrapper = CachingStoreWrapper.builder(core).caching(caching).build();
    return wrapper;
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
  public ConsulFeatureStoreBuilder host(String host) {
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
  public ConsulFeatureStoreBuilder port(int port) {
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
  public ConsulFeatureStoreBuilder url(URL url) {
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
  public ConsulFeatureStoreBuilder existingClient(Consul client) {
    this.existingClient = client;
    return this;
  }
  
  /**
   * Sets an optional namespace prefix for all keys stored in Consul. Use this if you are sharing
   * the same database table between multiple clients that are for different LaunchDarkly
   * environments, to avoid key collisions. 
   *
   * @param prefix the namespace prefix
   * @return the builder
   */
  public ConsulFeatureStoreBuilder prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  /**
   * Specifies whether local caching should be enabled and if so, sets the cache properties. Local
   * caching is enabled by default; see {@link FeatureStoreCacheConfig#DEFAULT}. To disable it, pass
   * {@link FeatureStoreCacheConfig#disabled()} to this method.
   * 
   * @param caching a {@link FeatureStoreCacheConfig} object specifying caching parameters
   * @return the builder
   */
  public ConsulFeatureStoreBuilder caching(FeatureStoreCacheConfig caching) {
    this.caching = caching;
    return this;
  }
}
