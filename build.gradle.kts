
group = (project.findProperty("GROUP") as String)
version = (project.findProperty("VERSION_NAME") as String)


plugins {
    kotlin("jvm") version "1.6.21"
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.14.2"
    id("org.jetbrains.dokka") version "1.6.21"
}

repositories {
    mavenCentral()
}

val ktor_version = "2.0.1"
val spek_version = "2.0.18"

dependencies {
    compile("io.ktor:ktor-server-core:$ktor_version")
    compile("com.graphql-java:graphql-java:18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation(kotlin("reflect"))

    testCompile("io.ktor:ktor-server-test-host:$ktor_version") {
        exclude(
                group = "ch.qos.logback",
                module = "logback-classic"
        )
    }
    testCompile(kotlin("test"))
    testCompile("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spek_version")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spek_version")
}


tasks {
    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}