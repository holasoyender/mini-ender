@file:Suppress("VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring") version "1.8.10"
}

group = "miniender"
version = "0.1-PRIVATE_ALPHA"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.4")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("net.dv8tion:JDA:5.0.0-beta.5")
    //implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10")
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation("org.json:json:20230227")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("redis.clients:jedis:4.3.1")

    implementation(kotlin("reflect"))
    implementation("commons-codec:commons-codec:1.15")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "18"
}

application {
    mainClass.set("MainKt")
}