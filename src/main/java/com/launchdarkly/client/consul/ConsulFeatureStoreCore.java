package com.launchdarkly.client.consul;

import com.launchdarkly.client.VersionedData;
import com.launchdarkly.client.VersionedDataKind;
import com.launchdarkly.client.utils.FeatureStoreCore;
import com.launchdarkly.client.utils.FeatureStoreHelpers;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.model.kv.Operation;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.kv.Verb;
import com.orbitz.consul.option.ImmutablePutOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
class ConsulFeatureStoreCore implements FeatureStoreCore {
  private static final Logger logger = LoggerFactory.getLogger(ConsulFeatureStoreCore.class);
  
  private final Consul client;
  private final String prefix;
  
  ConsulFeatureStoreCore(Consul client, String prefix) {
    this.client = client;
    this.prefix = (prefix == null) ? "" : prefix;
  }
  
  @Override
  public void close() throws IOException {
    client.destroy();
  }

  @Override
  public VersionedData getInternal(VersionedDataKind<?> kind, String key) {
    Optional<String> value = client.keyValueClient().getValueAsString(itemKey(kind, key));
    if (!value.isPresent()) {
      return null;
    }
    return FeatureStoreHelpers.unmarshalJson(kind, value.get());
  }

  @Override
  public Map<String, VersionedData> getAllInternal(VersionedDataKind<?> kind) {
    Map<String, VersionedData> itemsOut = new HashMap<>();
    for (String value: client.keyValueClient().getValuesAsString(kindKey(kind))) { 
      VersionedData item = FeatureStoreHelpers.unmarshalJson(kind, value);
      itemsOut.put(item.getKey(), item);
    }
    return itemsOut;
  }

  @Override
  public void initInternal(Map<VersionedDataKind<?>, Map<String, VersionedData>> allData) {
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
    for (Map.Entry<VersionedDataKind<?>, Map<String, VersionedData>> entry: allData.entrySet()) {
      VersionedDataKind<?> kind = entry.getKey();
      for (VersionedData item: entry.getValue().values()) {
        String json = FeatureStoreHelpers.marshalJson(item);
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
  public VersionedData upsertInternal(VersionedDataKind<?> kind, VersionedData newItem) {
    String key = itemKey(kind, newItem.getKey());
    String json = FeatureStoreHelpers.marshalJson(newItem);
    
    // We will potentially keep retrying indefinitely until someone's write succeeds
    while (true) {
      Optional<Value> oldValue = client.keyValueClient().getValue(key);
      VersionedData oldItem = oldValue.flatMap(v -> v.getValueAsString()).map(
          s -> FeatureStoreHelpers.unmarshalJson(kind, s)).orElse(null);
      
      // Check whether the item is stale. If so, don't do the update (and return the existing item to
      // FeatureStoreWrapper so it can be cached)
      if (oldItem != null && oldItem.getVersion() >= newItem.getVersion()) {
        return oldItem;
      }
      
      // Otherwise, try to write. We will do a compare-and-set operation, so the write will only succeed if
      // the key's ModifyIndex is still equal to the previous value returned by getEvenIfDeleted. If the
      // previous ModifyIndex was zero, it means the key did not previously exist and the write will only
      // succeed if it still doesn't exist.
      long modifyIndex = oldValue.map(v -> v.getModifyIndex()).orElse(0L);
      boolean success = client.keyValueClient().putValue(key, json, 0,
          ImmutablePutOptions.builder().cas(modifyIndex).build());
      if (success) {
        return newItem;
      }
      
      // If we failed, retry the whole shebang
      logger.debug("Concurrent modification detected, retrying");
    }
  }

  @Override
  public boolean initializedInternal() {
    return client.keyValueClient().getValue(initedKey()).isPresent();
  }
  
  private String kindKey(VersionedDataKind<?> kind) {
    return prefix + "/" + kind.getNamespace();
  }
  
  private String itemKey(VersionedDataKind<?> kind, String key) {
    return kindKey(kind) + "/" + key;
  }
  
  private String initedKey() {
    return prefix + "/$inited";
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
