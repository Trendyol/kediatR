group = "com.trendyol"

plugins {
  kotlin("jvm") version libs.versions.kotlin.get()
  java
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
}

version = properties["version"]!!

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
    }
  }

  java {
    withSourcesJar()
  }

  mavenPublishing {
    coordinates(groupId = rootProject.group.toString(), artifactId = project.name, version = rootProject.version.toString())
    publishToMavenCentral()
    pom {
      name.set(project.name)
      description.set(project.properties["projectDescription"].toString())
      url.set(project.properties["projectUrl"].toString())
      licenses {
        license {
          name.set(project.properties["licence"].toString())
          url.set(project.properties["licenceUrl"].toString())
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
        url.set(project.properties["projectUrl"].toString())
      }
    }
    signAllPublications()
  }
}
