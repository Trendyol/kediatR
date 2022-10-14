group = "com.trendyol"
version = "2.0.0"

plugins {
    kotlin("jvm") version "1.7.20"
    id("kediatr-publishing") apply false
    id("kediatr-signing")
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    jacoco
    id("jacoco-report-aggregation") apply true
}

subprojectsOf("project") {
    apply {
        plugin("kotlin")
        plugin("maven-publish")
        plugin("kediatr-publishing")
        plugin("jacoco")
        plugin("jacoco-report-aggregation")
    }

    jacoco {
        reportsDirectory.set(rootProject.buildDir.resolve("jacoco"))
    }

    dependencies {
        jacocoAggregation(project)
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
        finalizedBy(tasks.named<Copy>("testAggregateResults"))
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    tasks.create<Copy>("testAggregateResults") {
        from(tasks.test.get().reports.junitXml.outputLocation.get().asFile)
        into("${rootProject.buildDir}/reports/test")
        include("*.xml")
        dependsOn(tasks.test)
    }

    // tasks.check {
    //     dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
    // }
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

fun subprojectsOf(
    vararg parentProjects: String,
    action: Action<Project>,
) = subprojects.filter { parentProjects.contains(it.parent?.name) }.forEach { action(it) }
