import org.jetbrains.kotlin.gradle.dsl.JvmTarget

dependencies {
  api(projects.projects.kediatrCore)
  implementation(
    libs.spring.boot
      .get2x()
      .starter
  )
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation(
    libs.spring.boot
      .get2x()
      .starter.test
  )
}

// Published main artifacts stay JDK 11-compatible even though the build runs on JDK 17.
tasks.compileKotlin {
  compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

tasks.compileJava {
  options.release.set(11)
}
