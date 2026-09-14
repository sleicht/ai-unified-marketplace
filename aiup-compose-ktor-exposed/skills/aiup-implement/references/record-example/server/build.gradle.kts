plugins { kotlin("jvm") }

dependencies {
    implementation(project(":shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.1.1")
    implementation("org.jetbrains.exposed:exposed-java-time:1.1.1")
    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
    implementation("io.ktor:ktor-server-auth:3.4.0")
    implementation("io.insert-koin:koin-core:4.1.1")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.ktor:ktor-server-test-host:3.4.0")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("org.flywaydb:flyway-database-postgresql:11.14.1")
    testImplementation("com.zaxxer:HikariCP:6.3.0")
    testImplementation("org.postgresql:postgresql:42.7.7")
    testImplementation("io.insert-koin:koin-test:4.1.1")
    testImplementation("com.microsoft.playwright:playwright:1.58.0")
}

tasks.test { useJUnitPlatform { excludeTags("postgres", "browser") } }

tasks.register<Test>("postgresTest") {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform { includeTags("postgres") }
}

tasks.register<Test>("browserTest") {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    environment("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")
    systemProperty("example.site", rootProject.file("web/.kobweb/site").absolutePath)
    useJUnitPlatform { includeTags("browser") }
}

tasks.register<JavaExec>("installBrowser") {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "chromium")
}

tasks.withType<Test>().configureEach {
    testLogging { exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
}
