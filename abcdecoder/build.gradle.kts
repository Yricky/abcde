import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
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

                implementation(compose.material3)
                implementation(project(":modules:abcde"))
            }
        }

        jvmTest{
            dependencies {
            }
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "kra-ui"
            packageVersion = "1.0.0"
        }
    }
}
