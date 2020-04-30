# Change log

All notable changes to the LaunchDarkly Java SDK Consul integration will be documented in this file. This project adheres to [Semantic Versioning](http://semver.org).

## [2.0.0-rc1] - 2020-04-29

This beta release corresponds to the 5.0.0-rc1 prerelease of the Java SDK. It has the same functionality as 1.1.0, but has been updated for interface changes in the SDK.

### Changed:
- The package `com.launchdarkly.client.integrations` is now `com.launchdarkly.sdk.server.integrations`.

### Removed:
- The original configuration syntax that was deprecated in the 1.1.0 release has been removed. The newer syntax introduced in 1.1.0 is now the only way.

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
