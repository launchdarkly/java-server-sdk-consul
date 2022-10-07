# Change log

All notable changes to the LaunchDarkly Java SDK Consul integration will be documented in this file. This project adheres to [Semantic Versioning](http://semver.org).

## [4.0.0] - 2022-10-06
This release updates the package to use the new logging mechanism that was introduced in version [5.10.0](https://github.com/launchdarkly/java-server-sdk/releases/tag/5.10.0) of the LaunchDarkly Java SDK, so that log output from the Consul integration is handled in whatever way was specified by the SDK's logging configuration, instead of always using SLF4J.

This version of the package will not work with SDK versions earlier than 5.10.0; that is the only reason for the 4.0.0 major version increment. The functionality of the package is otherwise unchanged, and there are no API changes.

## [3.0.0] - 2020-06-02
This release is for use with versions 5.0.0 and higher of [`launchdarkly-java-server-sdk`](https://github.com/launchdarkly/java-server-sdk).

For more information about changes in the SDK database integrations, see the [4.x to 5.0 migration guide](https://docs-stg.launchdarkly.com/252/sdk/server-side/java/migration-4-to-5/).

### Changed:
- The entry point is now `com.launchdarkly.sdk.server.integrations.Consul` rather than `com.launchdarkly.client.integrations.Consul`.
- The SLF4J logger name is now `com.launchdarkly.sdk.server.LDClient.DataStore.Consul` rather than `com.launchdarkly.client.integrations.ConsulDataStoreImpl`.

### Removed:
- Removed the deprecated entry point `com.launchdarkly.client.consul.ConsulComponents`.


## [2.0.0] - 2020-06-02
_This release is an error and should not be used. It is a copy of 1.1.0._

## [1.1.0] - 2020-01-30
### Added:
- New classes `com.launchdarkly.client.integrations.Consul` and `com.launchdarkly.client.integrations.ConsulDataStoreBuilder`, which serve the same purpose as the previous classes but are designed to work with the newer persistent data store API introduced in [Java SDK 4.12.0](https://github.com/launchdarkly/java-server-sdk/releases/tag/4.12.0).

### Deprecated:
- The old interface in the `com.launchdarkly.client.integrations.consul` package.

## [1.0.1] - 2019-05-13
### Changed:
- Corresponding to the SDK package name change from `com.launchdarkly:launchdarkly-client` to `com.launchdarkly:launchdarkly-java-server-sdk`, this package is now called `com.launchdarkly:launchdarkly-java-server-sdk-consul-store`. The functionality of the package, including the package names and class names in the code, has not changed.

## [1.0.0] - 2019-01-14

Initial release.
