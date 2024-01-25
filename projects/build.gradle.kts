plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    jacoco
    `jacoco-report-aggregation`
    `test-report-aggregation`
    id("com.adarshr.test-logger") version "4.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

jacoco {
    reportsDirectory.set(rootProject.layout.buildDirectory.dir("reports/jacoco"))
}

subprojects {
    apply {
        plugin("com.adarshr.test-logger")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("jacoco")
    }

    kotlin {
        jvmToolchain(17)
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
            showStandardStreams = true
        }
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }
    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(false)
            csv.required.set(false)
        }
    }
}

tasks.create<JacocoReport>("jacocoRootReport") {
    this.group = "Reporting"
    subprojects.forEach { dependsOn(it.tasks.test) }
    subprojects.forEach {
        sourceSets(it.sourceSets.getByName("main"))
        executionData.from(it.layout.buildDirectory.file("jacoco/test.exec"))
    }
    reports {
        html.required.set(false)
        xml.required.set(true)
        csv.required.set(false)
    }
}

tasks.testAggregateTestReport {
    subprojects.forEach { dependsOn(it.tasks.test) }
    subprojects.map { it.tasks.test }.forEach { testResults.from(it) }
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("reports/tests"))
}
