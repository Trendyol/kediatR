dependencies {
  api(projects.projects.kediatrCore)
  implementation(libs.spring.boot.get3x().starter)
  implementation(libs.spring.boot.get3x().autoconfigure)
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation(libs.spring.boot.get3x().starter.test)
}
