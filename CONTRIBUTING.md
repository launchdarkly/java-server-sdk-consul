# Contributing to this library

The source code for this library is [here](https://github.com/launchdarkly/java-server-sdk-consul). We encourage pull-requests and other contributions from the community. Since this library is meant to be used in conjunction with the LaunchDarkly Java SDK, you may want to look at the [Java SDK source code](https://github.com/launchdarkly/java-server-sdk) and our [SDK contributor's guide](https://docs.launchdarkly.com/sdk/concepts/contributors-guide).

## Submitting bug reports and feature requests
 
The LaunchDarkly SDK team monitors the [issue tracker](https://github.com/launchdarkly/java-server-sdk-consul/issues) in this repository. Bug reports and feature requests specific to this project should be filed in the issue tracker. The SDK team will respond to all newly filed issues within two business days.
 
## Submitting pull requests
 
We encourage pull requests and other contributions from the community. Before submitting pull requests, ensure that all temporary or unintended code is removed. Don't worry about adding reviewers to the pull request; the LaunchDarkly SDK team will add themselves. The SDK team will acknowledge all pull requests within two business days.
 
## Build instructions
 
### Prerequisites
 
The library builds with [Gradle](https://gradle.org/) and should be built against Java 8.
 
### Building

To build the library without running any tests:
```
./gradlew jar
```

If you wish to clean your working directory between builds, you can clean it by running:
```
./gradlew clean
```

If you wish to use an interim build in another Maven/Gradle project such as [hello-java](https://github.com/launchdarkly/hello-java), you will likely want to publish the artifact to your local Maven repository so that your other project can access it.
```
./gradlew publishToMavenLocal
```

### Testing
 
To build the library and run all unit tests:
```
./gradlew test
```

The tests expect you to have Consul running locally. A simple way to do this is with Docker:

```bash
docker run -p 8500:8500 consul
```
