import org.jetbrains.kotlin.gradle.dsl.*

group = "com.trendyol"

plugins {
  kotlin("jvm") version libs.versions.kotlin.get()
  java
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.kover)
  alias(libs.plugins.testLogger)
}

version = properties["version"]!!

val kmpModules = listOf(
  projects.projects.kediatrCore.name,
  projects.projects.kediatrKoinStarter.name,
)

subprojectsOf("projects") {
  val isKmp = name in kmpModules

  apply {
    if (isKmp) {
      plugin("org.jetbrains.kotlin.multiplatform")
    } else {
      plugin("kotlin")
      plugin("java")
    }
    plugin(rootProject.libs.plugins.spotless.pluginId)
    plugin(rootProject.libs.plugins.maven.publish.pluginId)
    plugin(rootProject.libs.plugins.kover.pluginId)
    plugin(rootProject.libs.plugins.testLogger.pluginId)
  }

  configureKotlin(isKmp)

  spotless {
    kotlin {
      ktlint(rootProject.libs.ktlint.cli.get().version)
        .setEditorConfigPath(rootProject.layout.projectDirectory.file(".editorconfig"))
    }
  }

  kover {
    reports {
      filters {
        excludes {
          packages("com.trendyol.kediatr.testing")
        }
      }
    }
  }

  dependencies {
    kover(project)
  }

  // KMP modules handle sources jar automatically, JVM-only modules need explicit configuration
  if (!isKmp) {
    java {
      withSourcesJar()
    }

    dependencies {
      implementation(rootProject.libs.kotlinx.coroutines.core)
      testImplementation(rootProject.libs.kotlinx.coroutines.test)
      testImplementation(rootProject.libs.kotest.assertions.core)
      testImplementation(rootProject.libs.kotest.assertions.table)
      testImplementation(rootProject.libs.kotest.framework.engine)
      testImplementation(rootProject.libs.kotest.runner.junit5)
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
    configure<com.adarshr.gradle.testlogger.TestLoggerExtension> {
      setTheme("mocha")
      showStandardStreams = true
    }
    reports {
      junitXml.required.set(true)
      html.required.set(true)
    }
  }
}

subprojects.of("project", except = listOf(projects.projects.kediatrTesting.name)) {
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

fun Project.configureKotlin(isKmp: Boolean) {
  val kotlinExtension = if (isKmp) {
    extensions.getByType<KotlinMultiplatformExtension>()
  } else {
    extensions.getByType<KotlinJvmProjectExtension>()
  }

  kotlinExtension.apply {
    jvmToolchain(17)
    compilerOptions {
      val commonArgs = listOf("-Xskip-metadata-version-check", "-Xexpect-actual-classes")
      val args = if (isKmp) commonArgs else listOf("-Xjsr305=strict") + commonArgs
      freeCompilerArgs.addAll(args)
      allWarningsAsErrors.set(true)
    }
  }
}
