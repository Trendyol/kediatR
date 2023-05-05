group = "com.trendyol"
version = "2.0.0"

plugins {
    kotlin("jvm") version "1.8.21"
    id("kediatr-publishing") apply false
    id("kediatr-coverage")
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
    java
}

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
        implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0")
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
            jvmTarget = "11"
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

tasks.check {
    dependsOn(tasks.named("testCodeCoverageReport"))
}

fun subprojectsOf(
    vararg parentProjects: String,
    action: Action<Project>,
) = subprojects.filter { parentProjects.contains(it.parent?.name) }.forEach { action(it) }
