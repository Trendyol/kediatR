@file:Suppress("UnstableApiUsage")

rootProject.name = "kediatR"
include(
    "project:kediatr-core",
    "project:kediatr-koin-starter",
    "project:kediatr-quarkus-starter",
    "project:kediatr-spring-starter"
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }

        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }
    }
}
