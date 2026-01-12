plugins {
  kotlin("jvm")
}

dependencies {
  api(projects.projects.kediatrCore)
  api(kotlin("test-common"))
  api(kotlin("test-annotations-common"))
  api(kotlin("test-junit5"))
  implementation(libs.kotest.assertions.core)
  implementation(libs.kotlinx.coroutines.test)
}

