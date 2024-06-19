plugins {
    id("io.quarkus") version "3.12.0"
}

dependencies {
    api(project(":projects:kediatr-core"))
    implementation(platform("io.quarkus:quarkus-bom:3.12.0"))
    implementation("io.quarkus:quarkus-arc")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.1.0")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-jacoco")
}

tasks.test.configure {
    environment["QUARKUS_JACOCO_REPORT_LOCATION"] = "/build/jacoco"
    environment["QUARKUS_JACOCO_DATA_FILE"] = "build/jacoco/test.exec"
}
