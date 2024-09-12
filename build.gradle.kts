import java.util.*

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "nl.chimpgamer.ultimatejqmessages"
    version = "1.1.1-SNAPSHOT"

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

        compileOnly("dev.dejvokep:boosted-yaml:1.3.7")
        implementation("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.6.13")
        compileOnly("org.incendo:cloud-core:2.0.0")
        compileOnly("org.incendo:cloud-paper:2.0.0-beta.9")
        compileOnly("org.incendo:cloud-minecraft-extras:2.0.0-beta.9")
        compileOnly("org.incendo:cloud-kotlin-coroutines:2.0.0")
        compileOnly("org.incendo:cloud-kotlin-extensions:2.0.0")

        implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT") { isTransitive = false }

        compileOnly("me.clip:placeholderapi:2.11.3")

        compileOnly("org.jetbrains.exposed:exposed-core:0.54.0") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:0.54.0") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:0.54.0") {
            exclude("org.jetbrains.kotlin")
        }

        compileOnly("com.zaxxer:HikariCP:5.1.0")
        compileOnly("org.xerial:sqlite-jdbc:3.46.0.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.4.1")
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
        processResources {
            filesMatching("**/*.yml") {
                expand("version" to project.version)
            }
        }

        shadowJar {
            archiveFileName.set("UltimateJQMessages-${project.name.capitalizeWords()}-v${project.version}.jar")

            val shadedPackage = "nl.chimpgamer.ultimatejqmessages.shaded"
            relocate("io.github.rysefoxx.inventory", "$shadedPackage.ryseinventory")
            relocate("com.github.shynixn.mccoroutine", "$shadedPackage.mccoroutine")
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

fun String.capitalizeWords() = split("[ _]".toRegex()).joinToString(" ") { s -> s.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }