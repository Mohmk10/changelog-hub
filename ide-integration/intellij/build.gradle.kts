plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "io.github.mohmk10"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {

    implementation("org.yaml:snakeyaml:2.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")

    intellijPlatform {
        intellijIdeaCommunity("2023.3")
        bundledPlugins("org.jetbrains.plugins.yaml", "Git4Idea")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "io.github.mohmk10.changelog-hub"
        name = "Changelog Hub"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "233"
            untilBuild = "243.*"
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        useJUnitPlatform()
    }
}
