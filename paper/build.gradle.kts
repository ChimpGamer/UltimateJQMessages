repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.18.0") { isTransitive = false }
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.18.0") { isTransitive = false }

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")
}



tasks {
    jar {
        enabled = false
    }
}