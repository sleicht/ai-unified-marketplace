import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.varabyte.kobweb.application")
}

group = "example.web"

kotlin {
    configAsKobwebApplication(includeServer = false)
    sourceSets {
        jsMain.dependencies {
            implementation(project(":client"))
            implementation("com.varabyte.kobweb:kobweb-core:0.24.0")
            implementation("org.jetbrains.compose.html:html-core:1.10.0")
            implementation("io.ktor:ktor-client-js:3.4.0")
        }
    }
}

kobweb { app { index { description.set("AIUP record example") } } }
