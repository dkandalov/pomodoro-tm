import org.gradle.api.internal.HasConvention
import org.jetbrains.intellij.IntelliJPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    idea
    java
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.intellij") version "1.4.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

val SourceSet.kotlin: SourceDirectorySet
    get() = (this as HasConvention).convention.getPlugin<KotlinSourceSet>().kotlin

sourceSets {
    main {
        java.srcDirs("./src")
        kotlin.srcDirs("./src")
        resources.srcDirs("./resources")
    }
    test {
        kotlin.srcDirs("./test")
    }
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "11"
        apiVersion = "1.5"
        languageVersion = "1.5"
    }
}

configure<IntelliJPluginExtension> {
    // See https://www.jetbrains.com/intellij-repository/releases
    version.set(
        System.getenv().getOrDefault(
            "IJ_VERSION",
            "212.4746.92" // with kotlin 1.5.10 support
//            "LATEST-EAP-SNAPSHOT"
        )
    )
    pluginName.set("pomodoro")
    downloadSources.set(true)
    sameSinceUntilBuild.set(false)
    updateSinceUntilBuild.set(false)
}
