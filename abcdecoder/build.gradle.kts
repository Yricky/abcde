import com.google.gson.GsonBuilder
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

group = "me.yricky"
version = rootProject.version

repositories {
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
    google()
}

kotlin {
    jvm{
        withJava()
    }
    jvmToolchain(17)


    sourceSets {
//        val desktopMain by getting

//        val commonMain by getting{
//
//        }
        jvmMain{
            dependencies {
                // Note, if you develop a library, you should use compose.desktop.common.
                // compose.desktop.currentOs should be used in launcher-sourceSet
                // (in a separate module for demo project and in testMain).
                // With compose.desktop.common you will also lose @Preview functionality
                //    implementation(compose.desktop.currentOs){
                //        exclude("org.jetbrains.compose.material")
                //    }
                if(project.hasProperty("universal")){
                    println("universal = true")
                    implementation(compose.desktop.linux_x64)
                    implementation(compose.desktop.linux_arm64)
                    implementation(compose.desktop.windows_x64)
                    implementation(compose.desktop.macos_x64)
                    implementation(compose.desktop.macos_arm64)
                } else {
                    println("universal = false")
                    implementation(compose.desktop.currentOs)
                }

                // https://mvnrepository.com/artifact/com.formdev/flatlaf
                //    runtimeOnly("com.formdev:flatlaf:3.4.1")
                implementation("dev.dirs:directories:26")

                implementation(compose.material3)
                implementation(project(":modules:abcde"))
                implementation(project(":modules:resde"))
                implementation(project(":modules:hapde"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")

            }
        }

        jvmTest{
            dependencies {
            }
        }
    }
}

tasks{
    withType<org.gradle.jvm.tasks.Jar>() {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
    withType(ProcessResources::class){
        outputs.upToDateWhen { false }
        doLast {
            val gson = GsonBuilder().disableHtmlEscaping().create()
//            println("abcdecoderGenRes")
            val genFile = File(destinationDir,"generated")
            if(!genFile.exists()){
                genFile.mkdirs()
            }
            println("genDir:${genFile.path}")
            File(genFile,"properties").apply {
                delete()
            }.writeText(gson.toJson(mapOf<String,String>(
                "version" to "${project.version}"
            )))
        }
    }
}

compose.desktop {
    application {
        mainClass = "me.yricky.abcde.MainKt"
        buildTypes{
            release{
                proguard.isEnabled.set(false)
            }
        }

        nativeDistributions {
//            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "kra-ui"
            packageVersion = "${project.version}"
        }
    }
}
