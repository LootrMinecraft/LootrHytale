import fr.smolder.hytale.gradle.Patchline
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    java
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    id("fr.smolder.hytale.dev") version "0.1.0"
}

repositories {
    mavenCentral()
}

hytale {
    patchLine.set(Patchline.PRE_RELEASE)
    gameVersion.set("latest")
    autoUpdateManifest.set(true)
    minMemory.set("2G")
    maxMemory.set("4G")
    useAotCache.set(true)
    vineflowerVersion.set("1.11.2")
    decompileFilter.set(listOf("com/hypixel/**"))
    decompilerHeapSize.set("6G")
    includeDecompiledSources.set(true)

    manifest {
        group = "Lootr"
        name = "Lootr"
        version = project.version.toString()
        description = "Server-friendly loot for everyone!"

        author {
            name = "Noobanidus"
            email = "noobanidus@gmail.com"
            url = "https://noobanidus.com"
        }

        website = "https://noobanidus.com"
        serverVersion = "*"
        main = "noobanidus.mods.lootr.LootrPlugin"
        includesAssetPack = true
        disabledByDefault = false

        dependency("Hytale:BlockStateModule", "*")
        dependency("Hytale:BlockModule", "*")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("updatePluginManifest")
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("generateManifest")
}
