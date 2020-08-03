import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

plugins {
    `java-library`
}

repositories {
    maven {
        name = "Cloudburst"
        url = uri("https://repo.nukkitx.com/snapshot/")
    }
}

dependencies {
    compile(project(":worldedit-core"))
    compile(project(":worldedit-libs:cloudburst"))
    compile("cn.nukkit:nukkit:2.0.0-SNAPSHOT")
}

addJarManifest(includeClasspath = true)