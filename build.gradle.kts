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
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Драйвер MongoDB для реактивного взаимодействия
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.9.1")

    // OpenAPI для WebFlux
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2")

    // Валидация
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Маппинг с помощью MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // Для работы с датами и временем
    implementation("org.threeten:threeten-extra:1.6.0")

    // Тестирование
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.testcontainers:testcontainers:1.18.0")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("org.testcontainers:mongodb:1.18.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
    }
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport, jacocoTestCoverageVerification)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(jacocoTestReport)
    }
}

pmd {
    toolVersion = "6.55.0"
    ruleSets = listOf()
    ruleSetFiles = files("config/pmd/pmd.xml")
}


checkstyle {
    toolVersion = "10.16.0"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
}

jacoco {
    toolVersion = "0.8.12"
}
