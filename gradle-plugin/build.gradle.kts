plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.9.22"
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "io.github.mohmk10"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.mohmk10:changelog-hub-core:1.0.0")
    implementation("io.github.mohmk10:changelog-hub-openapi-parser:1.0.0")
    implementation("io.github.mohmk10:changelog-hub-graphql-parser:1.0.0")
    implementation("io.github.mohmk10:changelog-hub-grpc-parser:1.0.0")
    implementation("io.github.mohmk10:changelog-hub-asyncapi-parser:1.0.0")
    implementation("io.github.mohmk10:changelog-hub-spring-parser:1.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.assertj:assertj-core:3.25.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website.set("https://github.com/Mohmk10/changelog-hub")
    vcsUrl.set("https://github.com/Mohmk10/changelog-hub.git")
    plugins {
        create("changelogHub") {
            id = "io.github.mohmk10.changelog-hub"
            displayName = "Changelog Hub"
            description = "API Breaking Change Detector - Detects breaking changes in REST, GraphQL, gRPC, AsyncAPI and Spring Boot APIs"
            tags.set(listOf("api", "changelog", "breaking-changes", "openapi", "graphql", "grpc", "asyncapi"))
            implementationClass = "io.github.mohmk10.changeloghub.gradle.ChangelogHubPlugin"
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
