
group = (project.findProperty("GROUP") as String)
version = (project.findProperty("VERSION_NAME") as String)


plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.14.2"
    id("org.jetbrains.dokka") version "1.4.20"
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
