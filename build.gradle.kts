import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("org.springframework.boot") version "3.2.6"
    id("io.spring.dependency-management") version "1.1.4"
    id("pmd")
    id("jacoco")
    id("org.sonarqube") version "4.0.0.2929"
    id("checkstyle")
    id("net.ltgt.errorprone") version "3.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Реактивные Spring зависимости
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    //предназначен для работы с MongoDB в реактивном стиле.
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    //предоставляет поддержку для работы с MongoDB в синхронном (императивном) стиле.
     implementation("org.springframework.boot:spring-boot-starter-data-mongodb")


    implementation("org.mongodb:mongodb-driver-reactivestreams:4.11.2")
    implementation("org.mongodb:mongodb-driver-core:4.11.2")

    // OpenAPI для WebFlux
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    implementation("org.threeten:threeten-extra:1.6.0")
    errorprone("com.google.errorprone:error_prone_core:2.27.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.testcontainers:testcontainers:1.18.0")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("org.testcontainers:mongodb:1.18.0")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport, jacocoTestCoverageVerification)
    }

    jacocoTestReport {
        dependsOn(checkstyleTest, checkstyleMain, pmdMain, pmdTest)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(jacocoTestReport)
    }
}

jacoco {
    toolVersion = "0.8.12"
}
