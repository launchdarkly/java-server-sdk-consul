# Change log

All notable changes to the LaunchDarkly Java SDK Consul integration will be documented in this file. This project adheres to [Semantic Versioning](http://semver.org).

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
