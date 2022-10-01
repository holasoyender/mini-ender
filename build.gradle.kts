import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "miniender"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.20")
    implementation("ch.qos.logback:logback-classic:1.4.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}