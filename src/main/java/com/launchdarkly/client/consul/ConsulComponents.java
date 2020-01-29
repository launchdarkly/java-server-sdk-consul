package com.launchdarkly.client.consul;

/**
 * Deprecated entry point for the Consul data store. This is for use with the older feature store API
 * in Java SDK 4.11.x and below. For Java SDK 4.12 and above, use {@link com.launchdarkly.client.integrations.Consul}.
 * 
 * @deprecated Use {@link com.launchdarkly.client.integrations.Consul}.
 */
@Deprecated
public abstract class ConsulComponents {
  /**
   * Creates a builder for a Consul feature store. You can modify any of the store's properties with
   * {@link ConsulFeatureStoreBuilder} methods before adding it to your client configuration with
   * {@link com.launchdarkly.client.LDConfig.Builder#dataStore(com.launchdarkly.client.FeatureStoreFactory)}.
   * 
   * @return the builder
   * @deprecated Use {@link com.launchdarkly.client.integrations.Consul#dataStore()}
   */
  @Deprecated
  public static ConsulFeatureStoreBuilder consulFeatureStore() {
    return new ConsulFeatureStoreBuilder();
  }
}
