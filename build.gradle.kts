plugins {
    id("java")
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "3.2.4"
}

group = "vn.doan"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // MySQL JDBC driver
    implementation("mysql:mysql-connector-java:8.0.33")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20231013")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
