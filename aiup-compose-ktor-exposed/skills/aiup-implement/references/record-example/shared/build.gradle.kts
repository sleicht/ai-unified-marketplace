plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    js { browser() }
    sourceSets {
        commonMain.dependencies { api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0") }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
