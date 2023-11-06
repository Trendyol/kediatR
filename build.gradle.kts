group = "com.trendyol"

plugins {
    kotlin("jvm") version "1.9.20"
    id("kediatr-publishing") apply false
    id("kediatr-coverage")
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("com.palantir.git-version") version "3.0.0"
    id("com.adarshr.test-logger") version "4.0.0"
    java
    jacoco
    `jacoco-report-aggregation`
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
version = details.lastTag

jacoco {
    reportsDirectory.set(rootProject.buildDir.resolve("jacoco"))
}

subprojectsOf("project") {
    apply {
        plugin("kotlin")
        plugin("kediatr-publishing")
        plugin("kediatr-coverage")
        plugin("java")
        plugin("jacoco")
        plugin("com.adarshr.test-logger")
        plugin("jacoco-report-aggregation")
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        testImplementation(platform("org.junit:junit-bom:5.10.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
        testlogger {
            setTheme("mocha")
            this.showStandardStreams = true
        }
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
        }
    }
}

tasks.check {
    dependsOn(tasks.named("testCodeCoverageReport"))
}

fun subprojectsOf(
    vararg parentProjects: String,
    action: Action<Project>,
) = subprojects.filter { parentProjects.contains(it.parent?.name) }.forEach { action(it) }
