dependencies {
    api(project(":projects:kediatr-core"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.insert-koin:koin-core:3.5.6")
}

dependencies {
    testImplementation("io.insert-koin:koin-test:4.0.0")
    testImplementation("io.insert-koin:koin-test-junit5:4.0.0")
}
