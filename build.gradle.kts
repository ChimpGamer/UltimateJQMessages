import java.util.*

plugins {
    kotlin("jvm") version "1.9.10"
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

        maven("https://repo.networkmanager.xyz/repository/maven-public/") // NetworkManager repository
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("dev.dejvokep:boosted-yaml:1.3.1")
        implementation("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.6")
        compileOnly("cloud.commandframework:cloud-paper:1.8.3")
        compileOnly("cloud.commandframework:cloud-minecraft-extras:1.8.3")

        implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT") { isTransitive = false }

        compileOnly("me.clip:placeholderapi:2.11.3")

        compileOnly("org.jetbrains.exposed:exposed-core:0.42.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:0.42.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:0.42.1") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.xerial:sqlite-jdbc:3.42.0.1")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.2.0")
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

            val shadedPackage = "nl.chimpgamer.ultimatejqmessages.shaded"
            relocate("io.github.rysefoxx.inventory", "$shadedPackage.ryseinventory")
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s -> s.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }