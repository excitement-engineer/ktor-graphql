
group = (project.findProperty("GROUP") as String)
version = (project.findProperty("VERSION_NAME") as String)


plugins {
    kotlin("jvm") version "1.7.20"
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.14.2"
    id("org.jetbrains.dokka") version "1.7.20"
    `java-library`
}

repositories {
    mavenCentral()
}

val ktor_version = "2.1.3"
val spek_version = "2.0.18"

dependencies {
    api("io.ktor:ktor-server-core:$ktor_version")
    api("com.graphql-java:graphql-java:19.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    implementation(kotlin("reflect"))

    testImplementation("io.ktor:ktor-server-test-host:$ktor_version") {
        exclude(
                group = "ch.qos.logback",
                module = "logback-classic"
        )
    }
    testImplementation(kotlin("test"))
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spek_version")
    testImplementation("org.spekframework.spek2:spek-runner-junit5:$spek_version")
}


tasks {
    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}