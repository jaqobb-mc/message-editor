plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.gradleup.shadow") version "8.3.4"
}

group = "dev.jaqobb"
version = "2.5.6-SNAPSHOT"
description = "Edit in-game messages that were previously unmodifiable"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

bukkit {
    name = "MessageEditor"
    main = "dev.jaqobb.message_editor.MessageEditorPlugin"
    version = project.version as String
    apiVersion = "1.13"
    depend = listOf("ProtocolLib")
    softDepend = listOf("PlaceholderAPI")
    description = project.description
    author = "jaqobb"
    website = "https://jaqobb.dev"
    commands {
        create("message-editor") {
            description = "Message Editor main command"
            aliases = listOf("messageeditor")
        }
    }
}

tasks {
    shadowJar {
        exclude("com/cryptomorin/xseries/messages/*")
        exclude("com/cryptomorin/xseries/particles/*")
        exclude("com/cryptomorin/xseries/unused/*")
        exclude("com/cryptomorin/xseries/NMSExtras*")
        exclude("com/cryptomorin/xseries/NoteBlockMusic*")
        exclude("com/cryptomorin/xseries/ReflectionUtils*")
        exclude("com/cryptomorin/xseries/SkullCacheListener*")
        exclude("com/cryptomorin/xseries/SkullUtils*")
        exclude("com/cryptomorin/xseries/XBiome*")
        exclude("com/cryptomorin/xseries/XBlock*")
        exclude("com/cryptomorin/xseries/XEnchantment*")
        exclude("com/cryptomorin/xseries/XEntity*")
        exclude("com/cryptomorin/xseries/XItemStack*")
        exclude("com/cryptomorin/xseries/XPotion*")
        exclude("com/cryptomorin/xseries/XTag*")
        exclude("com/cryptomorin/xseries/XWorldBorder*")
        exclude("com/cryptomorin/xseries/abstractions/*")
        exclude("com/cryptomorin/xseries/profiles/**")
        exclude("com/cryptomorin/xseries/reflection/**")
        relocate("com.cryptomorin.xseries", "dev.jaqobb.message_editor.library.xseries")
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("com.github.cryptomorin:XSeries:11.3.0")
}
