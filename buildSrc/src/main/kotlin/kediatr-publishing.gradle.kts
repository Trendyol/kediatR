plugins {
    `maven-publish`
    signing
}
fun getProperty(
    projectKey: String,
    environmentKey: String,
): String? {
    return if (project.hasProperty(projectKey)) {
        project.property(projectKey) as? String?
    } else {
        System.getenv(environmentKey)
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
                // https://oss.sonatype.org
                // https://central.sonatype.org/publish/publish-guide/#releasing-to-central
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (rootProject.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = getProperty("nexus_username", "nexus_username")
                    password = getProperty("nexus_password", "nexus_password")
                }
            }
        }
    }

    val signingKey = getProperty(projectKey = "gpg.key", environmentKey = "gpg_private_key")
    val passPhrase = getProperty(projectKey = "gpg.passphrase", environmentKey = "gpg_passphrase")
    signing {
        if (passPhrase == null) logger.warn(
            "The passphrase for the signing key was not found. Either provide it as env variable 'gpg_passphrase' or as project property 'gpg_passphrase'. Otherwise the signing might fail!"
        )
        useInMemoryPgpKeys(signingKey, passPhrase)
        sign(publishing.publications)
    }
}
