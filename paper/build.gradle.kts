repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.17.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.17.0") { isTransitive = false }

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.2")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.2")
}

tasks {
    jar {
        enabled = false
    }
}