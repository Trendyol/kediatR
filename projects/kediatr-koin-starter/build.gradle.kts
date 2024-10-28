dependencies {
  api(projects.projects.kediatrCore)
  implementation(libs.org.reflections)
  implementation(libs.koin.core)
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation(libs.koin.test)
  testImplementation(libs.koin.test.junit5)
}
