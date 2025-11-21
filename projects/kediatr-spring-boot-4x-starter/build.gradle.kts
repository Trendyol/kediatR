dependencies {
  api(projects.projects.kediatrCore)
  implementation(libs.spring.boot.get4x().starter)
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation(libs.spring.boot.get4x().starter.test)
}
