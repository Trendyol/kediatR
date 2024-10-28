val springVersion = "2.7.18"
dependencies {
  api(project(":projects:kediatr-core"))
  implementation("org.springframework.boot:spring-boot-starter:$springVersion")
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
}

kotlin {
  jvmToolchain(11)
}
