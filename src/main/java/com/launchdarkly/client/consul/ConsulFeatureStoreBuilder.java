package com.launchdarkly.client.consul;

import com.launchdarkly.client.Components;
import com.launchdarkly.client.FeatureStore;
import com.launchdarkly.client.FeatureStoreCacheConfig;
import com.launchdarkly.client.FeatureStoreFactory;
import com.launchdarkly.client.LDConfig;
import com.launchdarkly.client.integrations.ConsulDataStoreBuilder;
import com.launchdarkly.client.integrations.PersistentDataStoreBuilder;
import com.launchdarkly.client.interfaces.DiagnosticDescription;
import com.launchdarkly.client.value.LDValue;
import com.orbitz.consul.Consul;

import java.net.URL;

/**
 * Deprecated builder class for the Redis-based persistent data store.
 * <p>
 * The replacement for this class is {@link com.launchdarkly.client.integrations.Consul}.
 * This class is retained for backward compatibility with older Java SDK versions and will be removed in a
 * future version. 
 * 
 * @deprecated Use {@link com.launchdarkly.client.integrations.Consul#dataStore()}
 */
@Deprecated
public class ConsulFeatureStoreBuilder implements FeatureStoreFactory, DiagnosticDescription {
  /**
   * The default value for {@link #prefix(String)}.
   */
  public static final String DEFAULT_PREFIX = "launchdarkly";
  
  private final PersistentDataStoreBuilder wrappedOuterBuilder;
  private final ConsulDataStoreBuilder wrappedBuilder;
  
  ConsulFeatureStoreBuilder() {
    wrappedBuilder = com.launchdarkly.client.integrations.Consul.dataStore();
    wrappedOuterBuilder = Components.persistentDataStore(wrappedBuilder);
  }
  
  @Override
  public FeatureStore createFeatureStore() {
    return wrappedOuterBuilder.createFeatureStore();
  }
  
  /**
   * Specifies the hostname of the Consul agent. By default, it is {@code localhost}.
   *
   * @param host the hostname of the Consul agent
   * @return the builder
   * @see #port(int)
   */
  public ConsulFeatureStoreBuilder host(String host) {
    wrappedBuilder.host(host);
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
    wrappedBuilder.port(port);
    return this;
  }

  /**
   * Specifies the Consul agent's location as a URL.
   * 
   * @param url the Consul agent URL
   * @return the builder
   */
  public ConsulFeatureStoreBuilder url(URL url) {
    wrappedBuilder.url(url);
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
    wrappedBuilder.existingClient(client);
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
  public ConsulFeatureStoreBuilder prefix(String prefix) {
    wrappedBuilder.prefix(prefix);
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
    wrappedOuterBuilder.cacheTime(caching.getCacheTime(), caching.getCacheTimeUnit());
    wrappedOuterBuilder.staleValuesPolicy(caching.getStaleValuesPolicy().toNewEnum());
    return this;
  }

  @Override
  public LDValue describeConfiguration(LDConfig config) {
    return LDValue.of("Consul");
  }
}
