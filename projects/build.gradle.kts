plugins {
  kotlin("jvm") version libs.versions.kotlin.get()
  alias(libs.plugins.testLogger)
  alias(libs.plugins.kover)
}

kover {
  reports {
    filters {
      excludes {
        androidGeneratedClasses()
        packages("**generated**")
        classes("**generated**")
      }
    }
  }
}
subprojects {
  apply {
    plugin(rootProject.libs.plugins.kover.pluginId)
    plugin(rootProject.libs.plugins.testLogger.pluginId)
  }

  kotlin {
    jvmToolchain(17)
    compilerOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      allWarningsAsErrors = true
    }
  }

  dependencies {
    implementation(rootProject.libs.kotlinx.coroutines.core)
  }

  dependencies {
    testImplementation(rootProject.libs.kotlinx.coroutines.test)
    testImplementation(rootProject.libs.kotest.property.jvm)
    testImplementation(rootProject.libs.kotest.datatests)
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
