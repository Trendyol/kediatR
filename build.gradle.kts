group = "com.trendyol"

plugins {
    kotlin("jvm") version "1.8.21"
    id("kediatr-publishing") apply false
    id("kediatr-coverage")
    id("org.jlleitschuh.gradle.ktlint") version "11.4.0"
    id("com.palantir.git-version") version "3.0.0"
    java
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
version = details.lastTag
// version = "3.0.0-SNAPSHOT"

jacoco {
    reportsDirectory.set(rootProject.buildDir.resolve("jacoco"))
}

subprojectsOf("project") {
    apply {
        plugin("kotlin")
        plugin("kediatr-publishing")
        plugin("kediatr-coverage")
        plugin("java")
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        jacocoAggregation(project(project.path))
        implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.1"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
        testImplementation(platform("org.junit:junit-bom:5.9.3"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        finalizedBy(tasks.named<Copy>("testAggregateResults"))
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.create<Copy>("testAggregateResults") {
        from(tasks.test.get().reports.junitXml.outputLocation.get().asFile)
        into("${rootProject.buildDir}/reports/test")
        include("*.xml")
        dependsOn(tasks.test)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(false)
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
