plugins {
    id("io.quarkus") version "2.13.0.Final"
}

dependencies {
    api(project(":project:kediatr-core"))
    implementation(platform("io.quarkus:quarkus-bom:2.13.0.Final"))
    implementation("io.quarkus:quarkus-arc")
    compileOnly("javax.enterprise:cdi-api:2.0")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.quarkus:quarkus-junit5")
}
