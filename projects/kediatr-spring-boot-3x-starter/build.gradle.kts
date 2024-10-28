val springVersion = "3.3.5"
dependencies {
  api(project(":projects:kediatr-core"))
  implementation("org.springframework.boot:spring-boot-starter:$springVersion")
  implementation("org.springframework.boot:spring-boot-autoconfigure:$springVersion")
}

dependencies {
  testImplementation(testFixtures(projects.projects.kediatrCore))
  testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
}
