import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder

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

buildscript {
    dependencies{
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
    }
}

val repository:Repository = RepositoryBuilder().setGitDir(File(rootDir,".git")).build()
val projectVersionCode = "0.1.0"

val version = run {
    val branch:String = repository.branch
    if(branch == "v/$projectVersionCode"){
        projectVersionCode
    } else {
        "$projectVersionCode-${branch}-${repository.findRef("HEAD").objectId.abbreviate(7).name()}"
    }
}

println("project dir: ${rootDir.absolutePath}")
println("version: $version")
println("java version: ${System.getProperty("java.version")}")

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
