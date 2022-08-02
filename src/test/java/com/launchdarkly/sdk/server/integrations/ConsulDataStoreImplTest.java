package com.launchdarkly.sdk.server.integrations;

import com.launchdarkly.sdk.server.interfaces.PersistentDataStoreFactory;
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
public class ConsulDataStoreImplTest extends PersistentDataStoreTestBase<ConsulDataStoreImpl> {
  @Override
  protected PersistentDataStoreFactory buildStore(String prefix) {
    return com.launchdarkly.sdk.server.integrations.Consul.dataStore().prefix(prefix);
  }
  
  @Override
  protected void clearAllData() {
    clearEverything();
  }
  
  public static void clearEverything() {
    Consul client = Consul.builder().build();
    try {
      client.keyValueClient().deleteKeys("/");
    } finally {
      client.destroy();
    }
  }
}
