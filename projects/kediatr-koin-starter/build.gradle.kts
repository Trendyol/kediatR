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
        api(projects.projects.kediatrCore)
        implementation(libs.koin.core)
      }
    }
    jvmTest {
       dependencies {
         implementation(projects.projects.kediatrTesting)
         implementation(libs.koin.test)
         implementation(libs.koin.test.junit5)
       }
    }
  }
}
