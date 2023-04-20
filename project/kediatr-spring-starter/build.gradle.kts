val springVersion = "2.7.11"
dependencies {
    api(project(":project:kediatr-core"))
    implementation("org.springframework.boot:spring-boot-starter:$springVersion")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
}
