plugins {
  alias(libs.plugins.quarkus)
  java
}

dependencies {
  api(projects.projects.kediatrCore)
  implementation(platform(libs.quarkusBom))
  implementation(libs.quarkus.arc)
  implementation(libs.jakarta.enterpise.cdi.api)
}

dependencies {
  testImplementation(projects.projects.kediatrTesting)
  testImplementation(libs.quarkus.junit5)
}

tasks.compileKotlin {
  mustRunAfter(tasks.compileQuarkusGeneratedSourcesJava, tasks.compileQuarkusTestGeneratedSourcesJava)
}

tasks.sourcesJar {
  mustRunAfter(tasks.compileQuarkusGeneratedSourcesJava)
}


