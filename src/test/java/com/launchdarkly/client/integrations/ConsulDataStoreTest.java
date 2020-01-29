package com.launchdarkly.client.integrations;

import com.launchdarkly.client.Components;
import com.launchdarkly.client.FeatureStore;
import com.launchdarkly.client.FeatureStoreDatabaseTestBase;
import com.orbitz.consul.Consul;

/**
 * Runs the standard database feature store test suite that's defined in the Java SDK.
 * <p>
 * Note that you must be running a local Consul instance on port 8500 to run these tests.
 * The simplest way to do this is:
 * <pre>
 *     docker run -p 8500:8500 consul agent -server -node=server-1 -bootstrap-expect=1 -client=0.0.0.0
 * </pre>
 */
@SuppressWarnings("javadoc")
public class ConsulDataStoreTest extends FeatureStoreDatabaseTestBase<FeatureStore> {

  public ConsulDataStoreTest(boolean cached) {
    super(cached);
  }
  
  @Override
  protected FeatureStore makeStore() {
    return outerBuilder(com.launchdarkly.client.integrations.Consul.dataStore()).createFeatureStore();
  }
  
  @Override
  protected FeatureStore makeStoreWithPrefix(String prefix) {
    return outerBuilder(com.launchdarkly.client.integrations.Consul.dataStore().prefix(prefix)).createFeatureStore();
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
  
  private PersistentDataStoreBuilder outerBuilder(ConsulDataStoreBuilder coreBuilder) {
    return Components.persistentDataStore(coreBuilder).cacheSeconds(cached ? 30 : 0);
  }
}
