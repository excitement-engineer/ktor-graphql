## Release to Bintray

To release a new version on Bintray, first ensure all tests pass with,
then bump the version in the `build.gradle` according to [semantic versioning](https://semver.org/_).

Next, publish a release to Bintray using the following command:

```
./gradlew bintrayUpload -DBINTRAY_USER=<YOUR_USER_NAME> -DBINTRAY_API_KEY=<YOUR_API_KEY>
```

Alternatively you can create a `gradle.properties` file and specify the following properties: 

```
bintrayUser=<YOUR_USER_NAME>
bintrayApiKey=<YOUR_API_KEY>
```

And then you can run

```
./gradlew bintrayUpload
```

Make sure to tag a release in git and update the release notes on Github.

Once published, add [release notes](https://github.com/excitement-engineer/ktor-graphql/tags).

