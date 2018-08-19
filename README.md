# Ktor Graphql

Easily serve GraphQL over http together with Ktor.


## Installation: 

### Maven

Add Bintray repository:

```
 <repository>
    <id>bintray-excitement-engineer-ktor-graphql</id>
    <name>bintray</name>
    <url>https://dl.bintray.com/excitement-engineer/ktor-graphql</url>
</repository>
```

Add dependency:

```
<dependency>
    <groupId>com.github.excitement-engineer</groupId>
    <artifactId>ktor-graphql</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

Add Bintray repository:

```
repositories {
    maven {
        url  "https://dl.bintray.com/excitement-engineer/ktor-graphql" 
    }
}
```

Add dependency:

```
compile 'com.github.excitement-engineer:ktor-graphql:${version}'
```

## Credit

This library is based heavily on the [express-graphql](https://github.com/graphql/express-graphql) library. 

The API and test cases in this library was based on express-graphql, credit goes to the authors of this library.

