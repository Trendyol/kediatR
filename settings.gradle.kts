@file:Suppress("UnstableApiUsage")

rootProject.name = "kediatR"
include(
    "projects:kediatr-core",
    "projects:kediatr-koin-starter",
    "projects:kediatr-quarkus-starter",
    "projects:kediatr-spring-boot-2x-starter",
    "projects:kediatr-spring-boot-3x-starter"
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
