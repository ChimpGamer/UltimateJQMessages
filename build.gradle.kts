import java.util.*

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "nl.chimpgamer.ultimatejqmessages"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.github.johnrengelman.shadow")
    }

    repositories {
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.networkmanager.xyz/repository/maven-public/") // RyseInventory Repository
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("dev.dejvokep:boosted-yaml:1.3.1")
        implementation("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.6")
        compileOnly("cloud.commandframework:cloud-paper:1.8.3")
        compileOnly("cloud.commandframework:cloud-minecraft-extras:1.8.3")

        compileOnly("me.clip:placeholderapi:2.11.3")

        compileOnly("org.jetbrains.exposed:exposed-core:0.41.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:0.41.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:0.41.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.xerial:sqlite-jdbc:3.42.0.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.1.4")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "17"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "17"
        }

        processResources {
            filesMatching("**/*.yml") {
                expand("version" to project.version)
            }
        }

        shadowJar {
            archiveFileName.set("UltimateJQMessages-${project.name.capitalizeWords()}-v${project.version}.jar")
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s -> s.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }