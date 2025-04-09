plugins {
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

group = "net.okocraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks.compileJava {
    options.release.set(21)
}

tasks.processResources {
    filesMatching(listOf("plugin.yml")) {
        expand("projectVersion" to version)
    }
}
