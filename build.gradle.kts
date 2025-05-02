import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("application")
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
    id("com.github.ben-manes.versions") version "0.52.0"
    //use the Checkstyle plugin
    checkstyle
    //use JaCoCo plugin
    jacoco
    id("io.freefair.lombok") version "8.13.1"
    id("org.sonarqube") version "6.1.0.5360"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

//setup entry point in our App
application {
    mainClass.set("hexlet.code.app.AppApplication")
}
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.5")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.4.5")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("net.datafaker:datafaker:2.4.3")
    implementation("org.instancio:instancio-junit:5.4.1")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(platform("org.junit:junit-bom:5.13.0-M2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.0-M2")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.0")
    runtimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStackTraces = true
        showCauses = true
        showStandardStreams = true
    }
}
//tasks.withType<JacocoReport> {
//    reports {
//        xml.required.set(true)
//    }
//}
tasks.jacocoTestReport { reports { xml.required.set(true) } }

sonar {
    properties {
        property("sonar.projectKey", "AlexSekret_java-project-99")
        property("sonar.organization", "alexsekret")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
