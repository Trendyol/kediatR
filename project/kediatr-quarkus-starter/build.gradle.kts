plugins {
    id("io.quarkus") version "2.15.2.Final"
}

dependencies {
    api(project(":project:kediatr-core"))
    implementation(platform("io.quarkus:quarkus-bom:2.15.1.Final"))
    implementation("io.quarkus:quarkus-arc")
    implementation("javax.enterprise:cdi-api:2.0")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.quarkus:quarkus-junit5")
}
