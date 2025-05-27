import java.util.*

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
}

allprojects {
    group = "nl.chimpgamer.ultimatejqmessages"
    version = "1.2.2"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.gradleup.shadow")
    }

    repositories {
        maven("https://repo.networkmanager.xyz/repository/maven-public/") // NetworkManager repository
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("dev.dejvokep:boosted-yaml:1.3.7")
        compileOnly("org.incendo:cloud-core:2.0.0")
        compileOnly("org.incendo:cloud-kotlin-coroutines:2.0.0")
        compileOnly("org.incendo:cloud-kotlin-extensions:2.0.0")

        implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT") { isTransitive = false }

        compileOnly("org.jetbrains.exposed:exposed-core:0.57.0") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-dao:0.57.0") {
            exclude("org.jetbrains.kotlin")
        }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:0.57.0") {
            exclude("org.jetbrains.kotlin")
        }

        compileOnly("com.zaxxer:HikariCP:6.2.1")
        compileOnly("org.xerial:sqlite-jdbc:3.47.1.0")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.1")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    kotlin {
        jvmToolchain(21)
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