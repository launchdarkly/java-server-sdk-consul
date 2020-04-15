# LaunchDarkly SDK for Java - Consul integration

[![Circle CI](https://circleci.com/gh/launchdarkly/java-server-sdk-consul.svg?style=shield)](https://circleci.com/gh/launchdarkly/java-server-sdk-consul)
[![Javadocs](http://javadoc.io/badge/com.launchdarkly/launchdarkly-server-sdk-consul-store.svg)](http://javadoc.io/doc/com.launchdarkly/launchdarkly-server-sdk-consul-store)

This library provides a Consul-backed persistence mechanism (data store) for the [LaunchDarkly Java SDK](https://github.com/launchdarkly/java-server-sdk), replacing the default in-memory data store. The Consul API implementation it uses is [`com.orbitz.consul:consul-client`](https://github.com/rickfast/consul-client).

This version of the library requires at least version 5.0.0 of the LaunchDarkly Java SDK. The minimum Java version is 8. For Java SDK 4.x, use the latest 1.x version of this library.

For more information, see also: [Using a persistent data store](https://docs.launchdarkly.com/v2.0/docs/using-a-persistent-feature-store).

## Quick setup

This assumes that you have already installed the LaunchDarkly Java SDK.

1. Add this library to your project (updating the version number to use the [latest release](https://github.com/launchdarkly/java-server-sdk-consul/releases)):

        <dependency>
          <groupId>com.launchdarkly</groupId>
          <artifactId>launchdarkly-java-server-sdk-consul-store</artifactId>
          <version>2.0.0</version>
        </dependency>

2. The Consul client library should be pulled in automatically if you do not specify a dependency for it. If you want to use a different version, you may add your own dependency:

        <dependency>
          <groupId>com.orbitz.consul</groupId>
          <artifactId>consul-client</artifactId>
          <version>1.3.0</version>
        </dependency>

3. Import the LaunchDarkly package and the package for this library:

        import com.launchdarkly.sdk.server.*;
        import com.launchdarkly.sdk.server.integrations.*;

4. When configuring your SDK client, add the Consul data store as a `persistentDataStore`. You may specify any custom Consul options using the methods of `ConsulDataStoreBuilder`. For instance, to customize the Consul hostname:
        
        LDConfig config = new LDConfig.Builder()
            .dataStore(
                Components.persistentDataStore(
                	Consul.dataStore().host("my-consul-host")
                )
            )
            .build();

By default, the store will try to connect to a local Consul instance on port 8500. There are methods in `ConsulDataStoreBuilder` for changing the configuration options. Alternatively, if you already have a fully configured Consul client object, you can tell LaunchDarkly to use that:

                Components.persistentDataStore(
                	Consul.dataStore().existingClient(myConsulClientInstance)
                )

## Caching behavior

The LaunchDarkly SDK has a standard caching mechanism for any persistent data store, to reduce database traffic. This is configured through the SDK's `PersistentDataStoreBuilder` class as described the SDK documentation. For instance, to specify a cache TTL of 5 minutes:

        LDConfig config = new LDConfig.Builder()
            .dataStore(
                Components.persistentDataStore(
                    Consul.dataStore()
                ).cacheTime(Duration.ofMinutes(5))
            )
            .build();

## About LaunchDarkly
 
* LaunchDarkly is a continuous delivery platform that provides feature flags as a service and allows developers to iterate quickly and safely. We allow you to easily flag your features and manage them from the LaunchDarkly dashboard.  With LaunchDarkly, you can:
    * Roll out a new feature to a subset of your users (like a group of users who opt-in to a beta tester group), gathering feedback and bug reports from real-world use cases.
    * Gradually roll out a feature to an increasing percentage of users, and track the effect that the feature has on key metrics (for instance, how likely is a user to complete a purchase if they have feature A versus feature B?).
    * Turn off a feature that you realize is causing performance problems in production, without needing to re-deploy, or even restart the application with a changed configuration file.
    * Grant access to certain features based on user attributes, like payment plan (eg: users on the ‘gold’ plan get access to more features than users in the ‘silver’ plan). Disable parts of your application to facilitate maintenance, without taking everything offline.
* LaunchDarkly provides feature flag SDKs for a wide variety of languages and technologies. Check out [our documentation](https://docs.launchdarkly.com/docs) for a complete list.
* Explore LaunchDarkly
    * [launchdarkly.com](https://www.launchdarkly.com/ "LaunchDarkly Main Website") for more information
    * [docs.launchdarkly.com](https://docs.launchdarkly.com/  "LaunchDarkly Documentation") for our documentation and SDK reference guides
    * [apidocs.launchdarkly.com](https://apidocs.launchdarkly.com/  "LaunchDarkly API Documentation") for our API documentation
    * [blog.launchdarkly.com](https://blog.launchdarkly.com/  "LaunchDarkly Blog Documentation") for the latest product updates
    * [Feature Flagging Guide](https://github.com/launchdarkly/featureflags/  "Feature Flagging Guide") for best practices and strategies
