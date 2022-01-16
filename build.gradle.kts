import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.3.3"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "me.byquanton.survivalplugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

dependencies {
    bukkitLibrary("com.google.code.gson","gson","2.8.9")
    bukkitLibrary("commons-io","commons-io","2.11.0")
    bukkitLibrary("org.apache.logging.log4j","log4j-core","2.17.0")
    bukkitLibrary("club.minnced","discord-webhooks","0.7.4")
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate","api","2.0-SNAPSHOT")
    /*compileOnly "com.mojang","brigadier","1.0.17"*/
    implementation("net.kyori","adventure-text-minimessage","4.1.0-SNAPSHOT")
    implementation("org.mongodb","mongodb-driver-sync","4.4.0")
    implementation("org.javacord","javacord","3.3.2")
    implementation("cloud.commandframework","cloud-paper","1.6.1")
    implementation("de.jeff_media","MorePersistentDataTypes","1.0.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

bukkit {
    main = "me.byquanton.survivalplugin.SurvivalPlugin"
    apiVersion = "1.18"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    author = "byquanton"
    depend = listOf("floodgate")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    shadowJar {
        fun reloc(pkg: String) = relocate(pkg, "me.byquanton.survivalplugin.dependency.$pkg")

        reloc("org.mongodb")
        reloc("cloud.commandframework")
        reloc("io.leangen.geantyref")
        reloc("de.jeff_media.morepersistentdatatypes")
    }

    reobfJar {
        outputJar.set(layout.buildDirectory.file("libs/SurvivalPlugin.jar"))
    }
}