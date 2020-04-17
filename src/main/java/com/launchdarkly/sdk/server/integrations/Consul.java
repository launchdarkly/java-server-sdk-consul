package com.launchdarkly.sdk.server.integrations;

/**
 * Integration between the LaunchDarkly SDK and Consul.
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
   * {@link com.launchdarkly.sdk.server.Components#persistentDataStore(com.launchdarkly.sdk.server.interfaces.PersistentDataStoreFactory)}
   * and set any desired caching options. Finally, pass the result to
   * {@link com.launchdarkly.sdk.server.LDConfig.Builder#dataStore(com.launchdarkly.sdk.server.interfaces.DataStoreFactory)}.
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
