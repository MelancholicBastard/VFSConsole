import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.compose") version "1.9.0-rc01" // Compose Multiplatform/ Desktop
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
}

group = "com.melancholicbastard"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}

compose.desktop {
    application {
        mainClass = "com.melancholicbastard.MainKt" // Укажите путь к вашей функции main, например, "com.yourcompany.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "VFSConsole"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}