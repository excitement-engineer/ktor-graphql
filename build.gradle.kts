import com.jfrog.bintray.gradle.BintrayExtension
import java.util.Date

val versionNo = "2.0.0"
group = "com.github.excitement-engineer"
version = versionNo


plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.2"
}

repositories {
    mavenCentral()
    jcenter()
}

val ktor_version = "1.3.2"
val spek_version = "2.0.5"

dependencies {
    compile("io.ktor:ktor-server-core:$ktor_version")
    implementation(kotlin("stdlib-jdk8"))
    compile("com.graphql-java:graphql-java:14.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.4.1")
    implementation(kotlin("reflect"))

    testCompile("io.ktor:ktor-server-test-host:$ktor_version") {
        exclude(
                group = "ch.qos.logback",
                module = "logback-classic"
        )
    }
    testCompile(kotlin("test"))
    testCompile("org.apache.logging.log4j:log4j-slf4j-impl:2.9.1")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spek_version")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spek_version")
}


tasks {
    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    artifacts {
        archives(sourcesJar)
        archives(jar)
    }
}

val publicationName = "ktorGraphQL"

publishing {
    publications.invoke {
        create<MavenPublication>(publicationName) {
            from(components["java"])
        }
    }
}

fun findProperty(name: String) = (project.findProperty(name) as String?) ?: System.getenv(name)

bintray {
    user = findProperty("bintrayUser")
    key = findProperty("bintrayApiKey")
    setPublications(publicationName)

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = project.name
        name = project.name
        setLicenses("MIT")
        vcsUrl = "https://github.com/excitement-engineer/ktor-graphql.git"

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = versionNo
            desc = "GraphQL over HTTP for Ktor"
            vcsTag = versionNo
            released = Date().toString()
        })

    })
}
