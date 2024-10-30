plugins {
  `java-test-fixtures`
}

val testFixturesImplementation: Configuration by configurations.getting {
  extendsFrom(configurations.implementation.get())
}

dependencies {
  testFixturesImplementation(libs.kotest.assertions.core)
  testFixturesImplementation(junitLibs.junitJupiterApi)
  testFixturesImplementation(libs.kotlinx.coroutines.test)
  testFixturesRuntimeOnly(junitLibs.junitJupiterEngine)
}

kotlin {
  jvmToolchain(11)
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
