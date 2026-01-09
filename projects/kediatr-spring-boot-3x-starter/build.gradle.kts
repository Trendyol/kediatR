dependencies {
  api(projects.projects.kediatrCore)
  implementation(libs.spring.boot.get3x().starter)
}

dependencies {
  testImplementation(projects.projects.kediatrTesting)
  testImplementation(libs.spring.boot.get3x().starter.test)
}
