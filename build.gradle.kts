import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring") version "1.7.21"
}

group = "miniender"
version = "0.1-PRIVATE_ALPHA"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("net.dv8tion:JDA:5.0.0-beta.1")
    //implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22")
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation("org.json:json:20220924")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.5.1")
    implementation(kotlin("reflect"))
    implementation("commons-codec:commons-codec:1.15")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "18"
}

application {
    mainClass.set("MainKt")
}