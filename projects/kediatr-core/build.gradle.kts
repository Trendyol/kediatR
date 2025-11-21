plugins {
  kotlin("multiplatform")
}

kotlin {
  jvm()

  js(IR) {
    browser()
    nodejs()
  }

  iosArm64()
  iosX64()
  iosSimulatorArm64()

  macosArm64()
  macosX64()

  linuxX64()
  linuxArm64()

  mingwX64()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    jvmTest {
      dependencies {
        implementation(projects.projects.kediatrTesting)
        implementation(libs.kotest.assertions.core)
        implementation(libs.kotest.framework.engine)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kotest.runner.junit5)
        implementation(kotlin("test"))
      }
    }
  }
}
