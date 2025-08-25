plugins {
  kotlin("jvm") version libs.versions.kotlin.get()
  alias(libs.plugins.testLogger)
  alias(libs.plugins.kover)
}

subprojects {
  apply {
    plugin(rootProject.libs.plugins.kover.pluginId)
    plugin(rootProject.libs.plugins.testLogger.pluginId)
  }

  kotlin {
    jvmToolchain(17)
    compilerOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict", "-Xskip-metadata-version-check")
      allWarningsAsErrors = true
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
    implementation(rootProject.libs.kotlinx.coroutines.core)
    kover(this@subprojects)
  }

  dependencies {
    testImplementation(rootProject.libs.kotlinx.coroutines.test)
    testImplementation(rootProject.libs.kotest.assertions.core)
    testImplementation(rootProject.libs.kotest.assertions.table)
    testImplementation(rootProject.libs.kotest.framework.engine)
    testImplementation(rootProject.libs.kotest.runner.junit5)
  }

  tasks.test {
    useJUnitPlatform()
    testlogger {
      setTheme("mocha")
      showStandardStreams = true
    }
    reports {
      junitXml.required.set(true)
      html.required.set(true)
    }
  }
}
