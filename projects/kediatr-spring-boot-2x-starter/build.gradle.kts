dependencies {
  api(projects.projects.kediatrCore)
  implementation(libs.spring.boot.get2x().starter)
}

dependencies {
  testImplementation(projects.projects.kediatrTesting)
  testImplementation(libs.spring.boot.get2x().starter.test)
}
