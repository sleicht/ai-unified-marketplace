plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    js { browser() }
    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            api("androidx.compose.runtime:runtime:1.10.2")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("io.ktor:ktor-client-core:3.4.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            implementation("io.ktor:ktor-client-mock:3.4.0")
        }
        jvmMain.dependencies {
            implementation("org.jetbrains.compose.material3:material3:1.9.0-beta03")
        }
        jsMain.dependencies { implementation("io.ktor:ktor-client-js:3.4.0") }
    }
}
