
plugins {
    kotlin("multiplatform")
    id("java")
    id("application")
}

group = "me.yricky"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvm{
        withJava()
    }
    jvmToolchain(17)


    sourceSets {
        jvmMain{
            dependencies {
                implementation(project(":modules:abcde"))
                implementation("com.google.code.gson:gson:2.8.9")
            }
        }

        jvmTest{
            dependencies {
            }
        }
    }
}

application{
    mainClass.set("me.yricky.oh.findstr.MainKt")
}

tasks {
    register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlinJvm", "processResources")) // We need this for Gradle optimization to work
        archiveClassifier.set("fat") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val contents = configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } + sourceSets.main.get().output
        from(contents)
    }
}