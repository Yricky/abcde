import groovy.util.Node
import groovy.util.NodeList

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

group = project.rootProject.group
version = project.rootProject.version

repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/public/")
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
//        val desktopMain by getting

//        val commonMain by getting{
//
//        }
        commonMain{

        }
        jvmMain{
            dependencies {
                api(project(":modules:common"))
            }
        }

        jvmTest{
            dependencies {
                implementation("junit:junit:4.13.1")
            }
        }
    }
}

//publishing{
//    publications{
//        getByName("jvm",MavenPublication::class){
//            pom.withXml {
//                ((asNode()["dependencies"] as NodeList).get(0) as Node).apply {
//                    appendNode("dependency").apply {
//                        appendNode("groupId", project.rootProject.group)
//                        appendNode("artifactId", "common-jvm")
//                        appendNode("version", project.rootProject.version)
//                    }
//                }
//            }
//        }
//    }
//}