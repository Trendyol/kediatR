plugins {
    id("maven-publish")
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
