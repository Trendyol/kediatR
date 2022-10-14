dependencies {
    testImplementation(kotlin("test"))
}
tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}
