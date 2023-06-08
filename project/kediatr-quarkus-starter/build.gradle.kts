plugins {
    id("io.quarkus") version "3.1.0.Final"
}

dependencies {
    api(project(":project:kediatr-core"))
    implementation(platform("io.quarkus:quarkus-bom:3.1.1.Final"))
    implementation("io.quarkus:quarkus-arc")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.quarkus:quarkus-junit5")
}
