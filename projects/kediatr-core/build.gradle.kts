@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `java-test-fixtures`
}

val testFixturesImplementation: Configuration = configurations.getByName("testFixturesImplementation") {
  extendsFrom(configurations.implementation.get())
}

dependencies {
  testFixturesImplementation(libs.kotest.assertions.core)
  testFixturesImplementation(libs.kotest.runner.junit5)
  testFixturesImplementation(junitLibs.junitJupiterApi)
  testFixturesImplementation(libs.kotlinx.coroutines.test)
  testFixturesRuntimeOnly(junitLibs.junitJupiterEngine)
}

testing {
  suites {
    named<JvmTestSuite>("test") {
      useJUnitJupiter(libs.versions.junit.get())
    }
  }
}

// Published main artifacts stay JDK 11-compatible even though the build runs on JDK 17.
tasks.compileKotlin {
  compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

tasks.compileJava {
  options.release.set(11)
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
afterEvaluate {
  javaComponent.withVariantsFromConfiguration(configurations["testFixturesSourcesElements"]) { skip() }
}
