import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    id("org.jetbrains.compose") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
    `maven-publish`
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/public/")
    google()
    gradlePluginPortal()
    mavenCentral()
}

val projectVersionCode = "0.1.0"

val branch = run {
    val ba = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git","rev-parse","--abbrev-ref","HEAD")
        standardOutput = ba
    }.rethrowFailure()
    String(ba.toByteArray()).trim()
}

val version = run {
    if(branch == "v/$projectVersionCode"){
        projectVersionCode
    } else {
        val ba = ByteArrayOutputStream()
        exec {
            commandLine = listOf("git","rev-parse","--short","HEAD")
            standardOutput = ba
        }.rethrowFailure()
        "$projectVersionCode-dev-${String(ba.toByteArray()).trim()}"
    }
}

println("version: $version")
project.rootProject.group = "io.github.yricky.oh"
project.rootProject.version = version

tasks{
    named("publishToMavenLocal"){
        dependsOn(getByPath(":modules:common:publishToMavenLocal"))
        dependsOn(getByPath(":modules:abcde:publishToMavenLocal"))
        dependsOn(getByPath(":modules:resde:publishToMavenLocal"))
        doLast {
            println("Done!\ngroupId: ${project.rootProject.group}\nversion: ${project.rootProject.version}")
        }
    }
}
