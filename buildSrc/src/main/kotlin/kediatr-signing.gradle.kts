plugins {
    id("maven-publish")
    signing
}

signing {
    sign(publishing.publications)
}
