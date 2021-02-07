import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

plugins {
    `java-library`
}

repositories {
    maven {
        name = "Cloudburst"
        url = uri("https://repo.opencollab.dev/snapshot/")
    }
    mavenLocal()
}

dependencies {
    compile(project(":worldedit-core"))
    compile(project(":worldedit-libs:cloudburst"))
    compile("org.cloudburstmc:cloudburst-server:0.0.1-SNAPSHOT")
    compile("com.google.code.gson:gson:2.8.6")
}

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("internalVersion" to project.ext["internalVersion"])
    }
}

addJarManifest(includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        include(dependency(":worldedit-core"))
        include(dependency("org.antlr:antlr4-runtime"))
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.bukkit.fastutil") {
            include(dependency("it.unimi.dsi:fastutil"))
        }
        include(dependency("com.google.code.gson:gson"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
