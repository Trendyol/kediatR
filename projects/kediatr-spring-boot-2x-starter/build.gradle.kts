dependencies {
  api(projects.projects.kediatrCore)
  implementation(libs.spring.boot.get2x().starter)
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation(libs.spring.boot.get2x().starter.test)
}

kotlin {
  jvmToolchain(11)
}
