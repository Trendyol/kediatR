import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.quarkus)
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

tasks.withType<KotlinCompile> {
  mustRunAfter(tasks.quarkusGenerateCode, tasks.quarkusGenerateCodeDev, tasks.quarkusGeneratedSourcesClasses)
}
