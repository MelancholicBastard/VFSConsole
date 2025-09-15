import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
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
//     Зависимости для сериализации JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "com.melancholicbastard.vfsconsole.app.MainKt" // Укажите путь к вашей функции main, например, "com.yourcompany.MainKt"
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

tasks.register<JavaExec>("runWithScript") {
    group = "application"
    description = "Run the application with script arguments"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.melancholicbastard.vfsconsole.app.MainKt"

//     Аргументы командной строки
    args = listOf("--script", "src/main/resources/test_script.vfs", "--vfs-path", "src/main/resources/test_vfs.json")
}

tasks.register<JavaExec>("runWithScript2") {
    group = "application"
    description = "Run the application with script arguments"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.melancholicbastard.vfsconsole.app.MainKt"

    args = listOf("--script", "src/main/resources/new_test_script.vfs", "--vfs-path", "src/main/resources/new_test_vfs.json")
}

tasks.register<JavaExec>("runWithScript3") {
    group = "application"
    description = "Run the application with script arguments"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.melancholicbastard.vfsconsole.app.MainKt"

    args = listOf("--script", "src/main/resources/new_new_test_script.vfs", "--vfs-path", "src/main/resources/test_vfs.json")
}