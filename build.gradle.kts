plugins {
    id("fabric-loom") version "1.19.4"
}

group = "io.github.italoseara"
version = "0.1.0"

base {
    archivesName = "axiom-marker-tools"
}

// Axiom is All Rights Reserved: never commit or redistribute its jar. Point `axiomJar` (in the
// gitignored gradle.properties) at a local copy of Axiom 6.1.x for MC 1.21.11.
val axiomJar = providers.gradleProperty("axiomJar").orNull
    ?: error("Set axiomJar in gradle.properties to a local Axiom-6.1.x-for-MC1.21.11.jar")

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.20.1")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11")

    modCompileOnly(files(axiomJar))
    modLocalRuntime(files(axiomJar))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
