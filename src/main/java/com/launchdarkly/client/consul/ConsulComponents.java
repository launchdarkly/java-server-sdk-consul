package com.launchdarkly.client.consul;

/**
 * Entry point for using the Consul feature store.
 */
public abstract class ConsulComponents {
  /**
   * Creates a builder for a Consul feature store. You can modify any of the store's properties with
   * {@link ConsulFeatureStoreBuilder} methods before adding it to your client configuration with
   * {@link com.launchdarkly.client.LDConfig.Builder#featureStoreFactory(com.launchdarkly.client.FeatureStoreFactory)}.
   * 
   * @return the builder
   */
  public static ConsulFeatureStoreBuilder consulFeatureStore() {
    return new ConsulFeatureStoreBuilder();
  }
}
