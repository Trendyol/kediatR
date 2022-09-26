group = "com.trendyol"
version = "2.0.0"

plugins {
    kotlin("jvm") version "1.7.10"
    id("maven-publish")
    signing
}

allprojects {
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

subprojects(
    "kediatr-core",
    "kediatr-koin-starter",
    "kediatr-quarkus-starter",
    "kediatr-spring-starter"
) {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    dependencies {
        implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        testLogging {
            showStandardStreams = true
            showCauses = true
            showStackTraces = true
            events("PASSED", "FAILED", "SKIPPED", "STANDARD_OUT")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("publish-${project.name}") {
                    groupId = rootProject.group.toString()
                    artifactId = project.name
                    version = rootProject.version.toString()
                    from(components["java"])
                    pom {
                        name.set(rootProject.name)
                        description.set(project.properties["projectDescription"].toString())
                        url.set(project.properties["projectUrl"].toString())
                        packaging = "jar"
                        licenses {
                            license {
                                name.set(project.properties["licence"].toString())
                                url.set(project.properties["licenceUrl"].toString())
                            }
                        }
                        developers {
                            developer {
                                id.set("canerpatir")
                                name.set("Caner Patir")
                                email.set("caner.patir@trendyol.com")
                            }
                            developer {
                                id.set("bilal-kilic")
                                name.set("Bilal Kilic")
                                email.set("bilal.kilic@trendyol.com")
                            }
                        }
                        scm {
                            connection.set("scm:git@github.com:Trendyol/kediatR.git")
                            developerConnection.set("scm:git:ssh://github.com:Trendyol/kediatR.git")
                            url.set(project.properties["projectUrl"].toString())
                        }
                    }
                }
            }

            repositories {
                maven {
                    // change to point to your repo, e.g. http://my.org/repo
                    val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    // url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                    url = uri(layout.buildDirectory.dir("mavenlocalpublish"))
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

fun subprojects(
    vararg projects: String,
    action: Action<Project>,
) = subprojects.filter { projects.contains(it.name) }.forEach { action(it) }
