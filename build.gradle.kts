import org.gradle.api.internal.HasConvention
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

buildscript {
    repositories {
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { setUrl("http://dl.bintray.com/jetbrains/intellij-plugin-service") }
    }
}
plugins {
    java
    idea
    kotlin("jvm") version "1.3.70"
    id("org.jetbrains.intellij") version "0.4.18"
}

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit:junit:4.12")
}

fun sourceRoots(block: SourceSetContainer.() -> Unit) = sourceSets.apply(block)
val SourceSet.kotlin: SourceDirectorySet get() = (this as HasConvention).convention.getPlugin<KotlinSourceSet>().kotlin

sourceRoots {
    getByName("main") {
        java.srcDirs("./src")
        kotlin.srcDirs("./src")
        resources.srcDirs("./resources")
    }
    getByName("test") {
        kotlin.srcDirs("./test")
    }
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "11"
        apiVersion = "1.3"
        languageVersion = "1.3"
    }
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

intellij {
    // See https://www.jetbrains.com/intellij-repository/releases
    val ideVersion = System.getenv().getOrDefault("IJ_VERSION",
        "201.6668.113"
//        "LATEST-EAP-SNAPSHOT"
    )
    println("Using ide version: $ideVersion")
    version = ideVersion
    pluginName = "pomodoro"
    downloadSources = true
    sameSinceUntilBuild = false
    updateSinceUntilBuild = false
}

val Any.kotlin: SourceDirectorySet get() = withConvention(KotlinSourceSet::class) { kotlin }
