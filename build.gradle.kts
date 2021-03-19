import org.gradle.internal.os.OperatingSystem

plugins {
    java
    application
}

repositories {
    mavenCentral()
}

@Suppress("INACCESSIBLE_TYPE")
val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX -> "natives-linux"
    OperatingSystem.WINDOWS -> "natives-windows"
    else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:3.2.3"))

    val parts = listOf(
            "lwjgl",
            "lwjgl-opengl",
            "lwjgl-glfw"
    )

    for (part in parts) {
        implementation("org.lwjgl:$part")
        runtimeOnly("org.lwjgl", part, classifier = lwjglNatives)
    }

    // Same version that Vivecraft (at least for 1.16) uses
    implementation("net.java.dev.jna:jna:4.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1") // Use JUnit Jupiter API for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1") // Use JUnit Jupiter Engine for testing.
}

application {
    // Define the main class for the application.
    mainClassName = "xyz.znix.graphicstest.Main"
}

val test by tasks.getting(Test::class) {
    // Use junit platform for unit tests
    useJUnitPlatform()
}
