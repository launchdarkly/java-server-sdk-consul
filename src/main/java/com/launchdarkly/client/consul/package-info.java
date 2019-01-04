/**
 * This package provides a Consul-backed feature store for the LaunchDarkly Java SDK.
 * <p>
 * For more details about how and why you can use a persistent feature store, see:
 * https://docs.launchdarkly.com/v2.0/docs/using-a-persistent-feature-store
 * <p>
 * To use the Consul feature store with the LaunchDarkly client, you will first obtain a
 * builder by calling {@link com.launchdarkly.client.consul.ConsulComponents#consulFeatureStore()}, then optionally
 * modify its properties, and then include it in your client configuration. For example:
 * 
 * <pre>
 * import com.launchdarkly.client.*;
 * import com.launchdarkly.client.consul.*;

 * ConsulFeatureStoreBuilder store = ConsulComponents.consulFeatureStore()
 *     .caching(FeatureStoreCacheConfig.enabled().ttlSeconds(30));
 * LDConfig config = new LDConfig.Builder()
 *     .featureStoreFactory(store)
 *     .build();
 * </pre>
 * 
 * The default Consul configuration uses an address of localhost:8500. To customize any
 * properties of Consul, you can use the methods on {@link com.launchdarkly.client.consul.ConsulFeatureStoreBuilder}.
 * <p>
 * If you are using the same Consul host as a feature store for multiple LaunchDarkly
 * environments, use the {@link com.launchdarkly.client.consul.ConsulFeatureStoreBuilder#prefix(String)}
 * option and choose a different prefix string for each, so they will not interfere with each
 * other's data. 
 */
package com.launchdarkly.client.consul;