group = "com.trendyol"

plugins {
  kotlin("jvm") version libs.versions.kotlin.get()
  java
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
}

version = property("version")!!

subprojectsOf("projects") {
  apply {
    plugin("kotlin")
    plugin("java")
    plugin(rootProject.libs.plugins.spotless.pluginId)
    plugin(rootProject.libs.plugins.maven.publish.pluginId)
  }

  spotless {
    kotlin {
      ktlint(rootProject.libs.ktlint.cli.get().version)
        .setEditorConfigPath(rootProject.layout.projectDirectory.file(".editorconfig"))
        .editorConfigOverride(
          mapOf(
            "ktlint_standard_when-entry-bracing" to "disabled"
          )
        )
    }

    kotlinGradle {
      ktlint(rootProject.libs.ktlint.cli.get().version)
    }
  }

  java {
    withSourcesJar()
  }

  // Build on JDK 17 — JUnit 6 / Kotest junit6 (used by every module's tests) require a
  // JVM 17 runtime. Modules whose published main artifacts must stay JDK 11-compatible
  // downgrade only their main bytecode to 11 in their own build script.
  kotlin {
    jvmToolchain(17)
  }

  mavenPublishing {
    coordinates(groupId = rootProject.group.toString(), artifactId = project.name, version = rootProject.version.toString())
    publishToMavenCentral()
    pom {
      name.set(project.name)
      description.set(project.property("projectDescription")!!.toString())
      url.set(project.property("projectUrl")!!.toString())
      licenses {
        license {
          name.set(project.property("licence")!!.toString())
          url.set(project.property("licenceUrl")!!.toString())
        }
      }
      developers {
        developer {
          id.set("osoykan")
          name.set("Oguzhan Soykan")
          email.set("oguzhan.soykan@trendyol.com")
        }
      }
      scm {
        connection.set("scm:git@github.com:Trendyol/kediatR.git")
        developerConnection.set("scm:git:ssh://github.com:Trendyol/kediatR.git")
        url.set(project.findProperty("projectUrl")!!.toString())
      }
    }
    signAllPublications()
  }
}
