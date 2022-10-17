plugins {
    base
    `jacoco-report-aggregation`
    `test-report-aggregation`
}

dependencies {
    jacocoAggregation(project(":project:kediatr-core"))
    jacocoAggregation(project(":project:kediatr-koin-starter"))
    jacocoAggregation(project(":project:kediatr-quarkus-starter"))
    jacocoAggregation(project(":project:kediatr-spring-starter"))
}
