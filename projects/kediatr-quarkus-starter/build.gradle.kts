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

// Workaround for Quarkus circular dependency issue
// See: https://github.com/quarkusio/quarkus/issues/29698
project.afterEvaluate {
  getTasksByName("quarkusGenerateCode", true).forEach { task ->
    task.setDependsOn(task.dependsOn.filterIsInstance<Provider<Task>>().filter { it.get().name != "processResources" })
  }
  getTasksByName("quarkusGenerateCodeDev", true).forEach { task ->
    task.setDependsOn(task.dependsOn.filterIsInstance<Provider<Task>>().filter { it.get().name != "processResources" })
  }
}
