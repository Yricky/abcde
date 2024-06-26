import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.yaml.snakeyaml.Yaml

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "me.yricky"
version = "1.0-SNAPSHOT"

buildscript {
    dependencies{
        classpath("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")
        classpath("com.squareup.okhttp3:okhttp:3.14.0")
    }
}

repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/public/")
}

kotlin {
    jvm{
        withJava()
    }
    jvmToolchain(17)


    sourceSets {
        commonMain{
            if(project.hasProperty("updateIsaDefine")){
                prepareIsaResource()
            }
            dependencies{
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
            }
        }

        jvmMain{
            dependencies {
//                api("com.charleskorn.kaml:kaml:0.58.0")
//                api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")
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

                nativeConfig.contains("linuxX64") -> {
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

fun KotlinSourceSet.prepareIsaResource(){
    println("prepareIsaResource")
    val gson = GsonBuilder().disableHtmlEscaping().create()
    val isaYaml = OkHttpClient.Builder().build()
        .newCall(Request.Builder().url("https://gitee.com/openharmony/arkcompiler_runtime_core/raw/master/isa/isa.yaml").build())
        .execute().body()?.string()
    val sourceDir = File("${this.resources.sourceDirectories.asPath}/abcde")
    if(!sourceDir.exists()){
        sourceDir.mkdirs()
    }
    val jsonIsa = File(sourceDir,"isa.json")
//    println(isaYaml)
    if(isaYaml == null){
        if(!jsonIsa.exists()){
            throw IllegalStateException("Cannot find isa define!")
        }
    } else {
        gson.toJson(Yaml().load<Any>(isaYaml)).let { jsonIsa.writeText(it) }
    }
}
