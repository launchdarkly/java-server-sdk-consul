package com.launchdarkly.client.integrations;

/**
 * Integration between the LaunchDarkly SDK and Consul.
 * <p>
 * This API uses the persistent data store model that was introduced in version 4.12.0 of the LaunchDarkly Java SDK.
 * If you are using an older Java SDK version, use {@link com.launchdarkly.client.consul.ConsulComponents}.
 * 
 * @since 1.1.0
 */
@SuppressWarnings("javadoc")
public abstract class Consul {
  private Consul() {}
  
  /**
   * Returns a builder object for creating a Consul-backed data store.
   * <p>
   * This object can be modified with {@link ConsulDataStoreBuilder} methods for any desired
   * custom Consul options. Then, pass it to
   * {@link com.launchdarkly.client.Components#persistentDataStore(com.launchdarkly.client.interfaces.PersistentDataStoreFactory)}
   * and set any desired caching options. Finally, pass the result to
   * {@link com.launchdarkly.client.LDConfig.Builder#dataStore(com.launchdarkly.client.FeatureStoreFactory)}.
   * For example:
   * 
   * <pre><code>
   *     LDConfig config = new LDConfig.Builder()
   *         .dataStore(
   *             Components.persistentDataStore(
   *                 Consul.dataStore().host("my-consul-host")
   *             ).cacheSeconds(15)
   *         )
   *         .build();
   * </code></pre>
   * 
   * @return a data store configuration object
   */
  public static ConsulDataStoreBuilder dataStore() {
    return new ConsulDataStoreBuilder();
  }
}
