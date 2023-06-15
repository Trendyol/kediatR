plugins {
    id("io.quarkus") version "3.1.1.Final"
}

dependencies {
    api(project(":project:kediatr-core"))
    implementation(platform("io.quarkus:quarkus-bom:3.1.2.Final"))
    implementation("io.quarkus:quarkus-arc")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-jacoco")
}

tasks.test.configure {
    environment["QUARKUS_JACOCO_REPORT_LOCATION"] = "jacoco"
    environment["QUARKUS_JACOCO_DATA_FILE"] = "test.exec"
    doLast {
        file("$buildDir/test.exec").renameTo(file("$buildDir/jacoco/test.exec"))
    }
}
