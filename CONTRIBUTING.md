## Release to Bintray

To release a new version on Bintray, first ensure all tests pass with,
then bump the version in the `build.gradle` according to [semantic versioning](https://semver.org/_).

Next, publish a release to Bintray using the following command:

```
./gradlew bintrayUpload -Dbintray.user=<YOUR_USER_NAME> -Dbintray.key=<YOUR_API_KEY>
```

Make sure to tag a release in git and update the release notes on Github.

Once published, add [release notes](https://github.com/excitement-engineer/ktor-graphql/tags).

