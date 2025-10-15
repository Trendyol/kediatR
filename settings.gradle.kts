@file:Suppress("UnstableApiUsage")

import dev.aga.gradle.versioncatalogs.Generator.generate
import dev.aga.gradle.versioncatalogs.GeneratorConfig

rootProject.name = "kediatR"
include(
  "projects:kediatr-core",
  "projects:kediatr-koin-starter",
  "projects:kediatr-quarkus-starter",
  "projects:kediatr-spring-boot-2x-starter",
  "projects:kediatr-spring-boot-3x-starter",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
  id("dev.aga.gradle.version-catalog-generator") version ("3.4.0")
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven {
      url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
      url = uri("https://repo.maven.apache.org/maven2/")
    }
  }

  versionCatalogs {
    generate("junitLibs") {
      fromToml("junitBom")
      using {
        aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
      }
    }
  }
}

