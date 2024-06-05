group = "com.trendyol"

plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    id("kediatr-publishing") apply false
    id("com.palantir.git-version") version "3.1.0"
    java
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
version = details.lastTag

subprojectsOf("projects") {
    apply {
        plugin("kotlin")
        plugin("kediatr-publishing")
        plugin("java")
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }
}
