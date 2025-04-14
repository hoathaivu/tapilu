plugins {
    id("java")
    id("io.freefair.lombok") version "8.13.1"

    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "3.4.4"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.poi:poi:5.4.1")
    implementation("org.apache.poi:poi-ooxml:5.4.1")
    implementation("org.jsoup:jsoup:1.19.1")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")

    //Google services
    implementation(platform("com.google.maps:google-maps-places-bom:0.30.0"))
    implementation(platform("com.google.maps:google-maps-routing-bom:1.45.0"))
    implementation("com.google.maps:google-maps-places")
    implementation("com.google.maps:google-maps-routing")
    //dependencies below are not controlled by 'com.google.cloud:libraries-bom'
    implementation("com.google.maps:google-maps-services:2.2.0") {
        //default version 3.0.0 contains CVE-2023-3635
        exclude("com.squareup.okio:okio-jvm")
    }
    implementation("com.squareup.okio:okio-jvm:3.10.0")
    implementation("org.slf4j:slf4j-simple:1.7.25")

    //Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    modules {
        module("org.springframework.boot:spring-boot-starter-logging") {
            replacedBy("org.springframework.boot:spring-boot-starter-log4j2", "Use Log4j2 instead of Logback")
        }
    }
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.integration:spring-integration-mail:6.4.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}