package com.launchdarkly.client.consul;

import com.launchdarkly.client.FeatureStore;
import com.launchdarkly.client.FeatureStoreCacheConfig;
import com.launchdarkly.client.FeatureStoreDatabaseTestBase;
import com.orbitz.consul.Consul;

/**
 * Runs the standard database feature store test suite that's defined in the Java SDK.
 * <p>
 * Note that you must be running a local DynamoDB instance on port 8000 to run these tests.
 * The simplest way to do this is:
 * <pre>
 *     docker run -p 8000:8000 amazon/dynamodb-local
 * </pre>
 */
public class ConsulFeatureStoreTest extends FeatureStoreDatabaseTestBase<FeatureStore> {

  public ConsulFeatureStoreTest(boolean cached) {
    super(cached);
  }
  
  @Override
  protected FeatureStore makeStore() {
    return baseBuilder().createFeatureStore();
  }
  
  @Override
  protected FeatureStore makeStoreWithPrefix(String prefix) {
    return baseBuilder().prefix(prefix).createFeatureStore();
  }
  
  @Override
  protected void clearAllData() {
    Consul client = Consul.builder().build();
    try {
      client.keyValueClient().deleteKeys("/");
    } finally {
      client.destroy();
    }
  }
  
  private ConsulFeatureStoreBuilder baseBuilder() {
    return ConsulComponents.consulFeatureStore()
        .caching(cached ? FeatureStoreCacheConfig.enabled().ttlSeconds(30) : FeatureStoreCacheConfig.disabled());
  }
}
