
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "me.yricky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
//    google()
}

//dependencies {
//    testImplementation ("junit:junit:4.13.1")
//    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")
//
//    // Note, if you develop a library, you should use compose.desktop.common.
//    // compose.desktop.currentOs should be used in launcher-sourceSet
//    // (in a separate module for demo project and in testMain).
//    // With compose.desktop.common you will also lose @Preview functionality
//}

kotlin {
    jvm{
        withJava()
    }
    jvmToolchain(17)


    sourceSets {
        commonMain{
            dependencies{
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
            }
        }

        jvmMain{
            dependencies {
                api("com.charleskorn.kaml:kaml:0.58.0")
                api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")
            }
        }

        jvmTest{
            dependencies {
                implementation("junit:junit:4.13.1")
            }
        }

        val nativeEnable = (extra["native.enable"] as String)
        val nativeConfig = (extra["native.config"] as String).split(' ')
        if(nativeEnable == "1"){
            println("native.config:${nativeConfig}")
            nativeMain{ }
            when{
                nativeConfig.contains("macosArm64") -> {
                    macosArm64 {
                        binaries {
                            sharedLib {
                                baseName = "abcde" // on Linux and macOS
                                // baseName = "libnative" // on Windows
                            }
                        }
                    }
                }

                nativeConfig.contains("macosArm64") -> {
                    linuxX64 {
                        binaries {
                            sharedLib {
                                baseName = "abcde" // on Linux and macOS
                                // baseName = "libnative" // on Windows
                            }
                        }
                    }
                }
            }
        }
    }
}
