
plugins {
    kotlin("jvm")
}

group = "me.yricky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
//    google()
}

dependencies {
    testImplementation ("junit:junit:4.13.1")

    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
}

kotlin {
    jvmToolchain(17)
}
