plugins {
  alias(libs.plugins.quarkus)
  java
}

// Quarkus 4 requires Java 21.
kotlin {
  jvmToolchain(21)
}

dependencies {
  api(projects.projects.kediatrCore)
  implementation(platform(libs.quarkusBom))
  implementation(libs.quarkus.arc)
  implementation(libs.jakarta.enterpise.cdi.api)
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation(libs.quarkus.junit5)
}

tasks.compileKotlin {
  mustRunAfter(tasks.compileQuarkusGeneratedSourcesJava, tasks.compileQuarkusTestGeneratedSourcesJava)
}

tasks.sourcesJar {
  mustRunAfter(tasks.compileQuarkusGeneratedSourcesJava)
}
