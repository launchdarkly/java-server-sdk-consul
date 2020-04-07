package com.launchdarkly.sdk.server.integrations;

import com.launchdarkly.sdk.server.interfaces.DataStoreTypes.DataKind;
import com.launchdarkly.sdk.server.interfaces.DataStoreTypes.FullDataSet;
import com.launchdarkly.sdk.server.interfaces.DataStoreTypes.ItemDescriptor;
import com.launchdarkly.sdk.server.interfaces.DataStoreTypes.KeyedItems;
import com.launchdarkly.sdk.server.interfaces.DataStoreTypes.SerializedItemDescriptor;
import com.launchdarkly.sdk.server.interfaces.PersistentDataStore;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.model.kv.Operation;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.kv.Verb;
import com.orbitz.consul.option.ImmutablePutOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Internal implementation of the Consul feature store.
 * <p>
 * Implementation notes:
 * <ul>
 * <li> Feature flags, segments, and any other kind of entity the LaunchDarkly client may wish
 * to store, are stored as individual items with the key "{prefix}/features/{flag-key}",
 * "{prefix}/segments/{segment-key}", etc.
 * <li> The special key "{prefix}/$inited" indicates that the store contains a complete data set.
 * <li> Since Consul has limited support for transactions (they can't contain more than 64
 * operations), the Init method-- which replaces the entire data store-- is not guaranteed to
 * be atomic, so there can be a race condition if another process is adding new data via
 * Upsert. To minimize this, we don't delete all the data at the start; instead, we update
 * the items we've received, and then delete all other items. That could potentially result in
 * deleting new data from another process, but that would be the case anyway if the Init
 * happened to execute later than the Upsert; we are relying on the fact that normally the
 * process that did the Init will also receive the new data shortly and do its own Upsert.
 * </ul>
 */
class ConsulDataStoreImpl implements PersistentDataStore {
  private static final Logger logger = LoggerFactory.getLogger(ConsulDataStoreImpl.class);
  
  private final Consul client;
  private final String prefix;
  
  ConsulDataStoreImpl(Consul client, String prefix) {
    this.client = client;
    this.prefix = prefix + "/";
  }
  
  @Override
  public void close() throws IOException {
    client.destroy();
  }

  @Override
  public SerializedItemDescriptor get(DataKind kind, String key) {
    Optional<String> value = client.keyValueClient().getValueAsString(itemKey(kind, key));
    return value.map(s -> new SerializedItemDescriptor(0, false, s)).orElse(null);
  }

  @Override
  public KeyedItems<SerializedItemDescriptor> getAll(DataKind kind) {
    String baseKey = kindKey(kind);
    List<Value> values = client.keyValueClient().getValues(baseKey);
    List<Map.Entry<String, SerializedItemDescriptor>> itemsOut = new ArrayList<>(values.size());
    for (Value value: values) {
      String key = value.getKey().substring(baseKey.length() + 1);
      itemsOut.add(new AbstractMap.SimpleEntry<>(key,
          new SerializedItemDescriptor(0, false, value.getValueAsString().orElse(null))));
    }
    return new KeyedItems<>(itemsOut);
  }

  @Override
  public void init(FullDataSet<SerializedItemDescriptor> allData) {
    // Start by reading the existing keys; we will later delete any of these that weren't in allData.
    Set<String> unusedOldKeys = new HashSet<>();
    try {
      unusedOldKeys.addAll(client.keyValueClient().getKeys(prefix));
    } catch (ConsulException e) {
      // Annoyingly, if no keys currently exist, the client throws an exception instead of just returning an empty list
      if (e.getCode() != 404) {
        throw e;
      }
    }

    List<Operation> ops = new ArrayList<>();
    int numItems = 0;
    
    // Insert or update every provided item
    for (Map.Entry<DataKind, KeyedItems<SerializedItemDescriptor>> entry: allData.getData()) {
      DataKind kind = entry.getKey();
      for (Map.Entry<String, SerializedItemDescriptor> item: entry.getValue().getItems()) {
        String json = jsonOrPlaceholder(kind, item.getValue());
        String key = itemKey(kind, item.getKey());
        Operation op = Operation.builder(Verb.SET).key(key).value(json).build();
        ops.add(op);
        unusedOldKeys.remove(key);
        numItems++;
      }
    }
    
    // Now delete any previously existing items whose keys were not in the current data
    for (String key: unusedOldKeys) {
      if (!key.equals(initedKey())) {
        Operation op = Operation.builder(Verb.DELETE).key(key).build();
        ops.add(op);
      }
    }
    
    // Now set the special key that we check in initializedInternal()
    Operation op = Operation.builder(Verb.SET).key(initedKey()).value("").build();
    ops.add(op);
    
    batchOperations(ops);
    
    logger.info("Initialized database with {} items", numItems);
  }

  @Override
  public boolean upsert(DataKind kind, String key, SerializedItemDescriptor newItem) {
    String consulKey = itemKey(kind, key);
    String json = jsonOrPlaceholder(kind, newItem);
    
    // We will potentially keep retrying indefinitely until someone's write succeeds
    while (true) {
      Optional<Value> oldValue = client.keyValueClient().getValue(key);
      int oldVersion = oldValue.flatMap(v -> v.getValueAsString())
          .map(j -> kind.deserialize(j).getVersion()).orElse(-1);
      
      // Check whether the item is stale. If so, don't do the update.
      if (oldVersion >= newItem.getVersion()) {
        return false;
      }
      
      // Otherwise, try to write. We will do a compare-and-set operation, so the write will only succeed if
      // the key's ModifyIndex is still equal to the previous value returned by getEvenIfDeleted. If the
      // previous ModifyIndex was zero, it means the key did not previously exist and the write will only
      // succeed if it still doesn't exist.
      long modifyIndex = oldValue.map(v -> v.getModifyIndex()).orElse(0L);
      boolean success = client.keyValueClient().putValue(consulKey, json, 0,
          ImmutablePutOptions.builder().cas(modifyIndex).build());
      if (success) {
        return true;
      }
      
      // If we failed, retry the whole shebang
      logger.debug("Concurrent modification detected, retrying");
    }
  }

  @Override
  public boolean isInitialized() {
    return client.keyValueClient().getValue(initedKey()).isPresent();
  }

  @Override
  public boolean isStoreAvailable() {
    try {
      isInitialized(); // don't care about the return value, just that it doesn't throw an exception
      return true;
    } catch (Exception e) { // don't care about exception class, since any exception means the Consul request couldn't be made
      return false;
    }
  }
  
  private String kindKey(DataKind kind) {
    return prefix + kind.getName();
  }
  
  private String itemKey(DataKind kind, String key) {
    return kindKey(kind) + "/" + key;
  }
  
  private String initedKey() {
    return prefix + "$inited";
  }

  private static String jsonOrPlaceholder(DataKind kind, SerializedItemDescriptor serializedItem) {
    String s = serializedItem.getSerializedItem();
    if (s != null) {
      return s;
    }
    // For backward compatibility with previous implementations of the Consul integration, we must store a
    // special placeholder string for deleted items. DataKind.serializeItem() will give us this string if
    // we pass a deleted ItemDescriptor.
    return kind.serialize(ItemDescriptor.deletedItem(serializedItem.getVersion()));
  }
  
  private void batchOperations(List<Operation> ops) {
    int batchSize = 64; // Consul can only do this many at a time 
    for (int i = 0; i < ops.size(); i += batchSize) {
      int limit = (i + batchSize < ops.size()) ? (i + batchSize) : ops.size();
      List<Operation> batch = ops.subList(i, limit);
      client.keyValueClient().performTransaction(batch.toArray(new Operation[batch.size()]));
    }
  }
}
