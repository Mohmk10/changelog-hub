plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.9.22"
}

group = "io.github.mohmk10"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.mohmk10:core:1.0.0-SNAPSHOT")
    implementation("io.github.mohmk10:openapi-parser:1.0.0-SNAPSHOT")
    implementation("io.github.mohmk10:graphql-parser:1.0.0-SNAPSHOT")
    implementation("io.github.mohmk10:grpc-parser:1.0.0-SNAPSHOT")
    implementation("io.github.mohmk10:asyncapi-parser:1.0.0-SNAPSHOT")
    implementation("io.github.mohmk10:spring-parser:1.0.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.assertj:assertj-core:3.25.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("changelogHub") {
            id = "io.github.mohmk10.changelog-hub"
            implementationClass = "io.github.mohmk10.changeloghub.gradle.ChangelogHubPlugin"
            displayName = "Changelog Hub Plugin"
            description = "API breaking change detector with automatic changelog generation"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "changelog-hub-gradle-plugin"
        }
    }
}
