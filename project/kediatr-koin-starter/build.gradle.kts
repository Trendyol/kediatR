val koinVersion = "3.3.3"

dependencies {
    api(project(":project:kediatr-core"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.insert-koin:koin-core:$koinVersion")
}

dependencies {
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")
}
