## Release to Maven Central

To release a new version to Maven Central make sure to set the credentials for OSSRH following gradle properties in `USER_HOME/.gradle/gradle.properties`

```
mavenCentralRepositoryUsername=username
mavenCentralRepositoryPassword=pass
```

Also set the keys for signing the jars in `USER_HOME/.gradle/gradle.properties`:

```
signing.keyId=3123213
signing.password=123
signing.secretKeyRingFile=/Users/user/.gnupg/secring.gpg
```

To create the keys install [GnuPG](https://www.gnupg.org/download/) using `brew install gnupg`:

```
gpg --full-generate-key
``` 

Then export the keys to a file:

```
gpg --export-secret-keys -o /Users/user/.gnupg/secring.gpg
```

The key id is retrieved using:

```
gpg --list-keys --keyid-format SHORT
```

Make sure to send the key to the key server:

```
gpg --send-keys --keyserver keyserver.ubuntu.com <KEY ID>
```


To publish to staging run the `publish` gradle task.

Next login to the nexus [website](https://oss.sonatype.org) and close the repository.

After closing it is staged and can be tested by adding the repository `https://oss.sonatype.org/content/repositories/staging/` 
in the build.gradle using:

```
repositories {
    mavenCentral()
    maven {
        url = uri("https://...")
    }
}
```

Once it works then release it using the nexus website by **closing** the staging.

Make sure to tag a release in git and update the release notes on Github. 

Once published, add [release notes](https://github.com/excitement-engineer/ktor-graphql/tags).

