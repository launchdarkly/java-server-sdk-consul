package com.launchdarkly.client.consul;

import com.launchdarkly.client.FeatureStore;
import com.launchdarkly.client.FeatureStoreCacheConfig;
import com.launchdarkly.client.FeatureStoreDatabaseTestBase;
import com.launchdarkly.client.integrations.ConsulDataStoreImplTest;

/**
 * Runs the standard database feature store test suite that's defined in the Java SDK.
 * <p>
 * Note that you must be running a local Consul instance on port 8500 to run these tests.
 * The simplest way to do this is:
 * <pre>
 *     docker run -p 8500:8500 consul agent -server -node=server-1 -bootstrap-expect=1 -client=0.0.0.0
 * </pre>
 */
@SuppressWarnings({ "deprecation", "javadoc" })
public class DeprecatedConsulFeatureStoreTest extends FeatureStoreDatabaseTestBase<FeatureStore> {

  public DeprecatedConsulFeatureStoreTest(boolean cached) {
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
    ConsulDataStoreImplTest.clearEverything();
  }
  
  private ConsulFeatureStoreBuilder baseBuilder() {
    return ConsulComponents.consulFeatureStore()
        .caching(cached ? FeatureStoreCacheConfig.enabled().ttlSeconds(30) : FeatureStoreCacheConfig.disabled());
  }
}
