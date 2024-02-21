val springVersion = "3.2.2"
dependencies {
    api(project(":projects:kediatr-core"))
    implementation("org.springframework.boot:spring-boot-starter:$springVersion")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
}

kotlin {
    jvmToolchain(11)
}
