group = "com.trendyol"

plugins {
  kotlin("jvm") version libs.versions.kotlin.get()
  java
  id("kediatr-publishing") apply false
  alias(libs.plugins.spotless)
}

version = properties["version"]!!

subprojectsOf("projects") {
  apply {
    plugin("kotlin")
    plugin("kediatr-publishing")
    plugin("java")
    plugin(rootProject.libs.plugins.spotless.pluginId)
  }

  spotless {
    kotlin {
      ktlint()
        .setEditorConfigPath(rootProject.layout.projectDirectory.file(".editorconfig"))
    }
  }

  java {
    withSourcesJar()
    withJavadocJar()
  }
}
