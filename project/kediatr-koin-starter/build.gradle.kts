dependencies {
    api(project(":project:kediatr-core"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.insert-koin:koin-core:3.5.2")
}

dependencies {
    testImplementation("io.insert-koin:koin-test:3.5.2")
    testImplementation("io.insert-koin:koin-test-junit5:3.5.2")
}
