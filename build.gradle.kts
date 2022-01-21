import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.kalimero2.team.survivalplugin"
version = "2.2-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

dependencies {
    bukkitLibrary("com.google.code.gson","gson","2.8.9")
    bukkitLibrary("commons-io","commons-io","2.11.0")
    bukkitLibrary("org.apache.logging.log4j","log4j-core","2.17.0")
    bukkitLibrary("club.minnced","discord-webhooks","0.7.4")
    compileOnly("io.papermc.paper","paper-api","1.18.1-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate","api","2.0-SNAPSHOT")
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
    main = "com.kalimero2.team.survivalplugin.SurvivalPlugin"
    apiVersion = "1.18"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("byquanton")
    depend = listOf("floodgate")
}



tasks {

    shadowJar {
        fun reloc(pkg: String) = relocate(pkg, "com.kalimero2.team.survivalplugin.dependency.$pkg")

        reloc("org.mongodb")
        reloc("cloud.commandframework")
        reloc("io.leangen.geantyref")
        reloc("de.jeff_media.morepersistentdatatypes")

        archiveFileName.set("SurvivalPlugin.jar")
    }

    jar{

        dependsOn(shadowJar)
    }
}