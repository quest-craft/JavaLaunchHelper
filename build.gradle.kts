import org.gradle.internal.os.OperatingSystem
import java.net.URI

plugins {
    java
    application
}

repositories {
    mavenCentral()
    maven {
        url = URI("https://libraries.minecraft.net/")
    }
}

@Suppress("INACCESSIBLE_TYPE")
val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX -> "natives-linux"
    OperatingSystem.WINDOWS -> "natives-windows"
    else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}

val inJar by configurations.creating
val inJarOnly by configurations.creating

dependencies {
    val lwjglVersion = "2.9.3"

    implementation("org.lwjgl.lwjgl:lwjgl:$lwjglVersion")
    //runtimeOnly("org.lwjgl.lwjgl", "lwjgl", lwjglVersion, classifier = lwjglNatives)
    implementation("org.lwjgl.lwjgl:lwjgl_util:$lwjglVersion")

    // All inJar dependencies are also implementation dependencies
    implementation(inJar)
    runtimeOnly(inJarOnly)

    // The Minecraft launchwrapper which is available at runtime
    // Implementation is fine for these, since only inJar files are emitted when sent to the device
    implementation("net.minecraft:launchwrapper:1.12")
    implementation("org.ow2.asm:asm-debug-all:5.2") // AFAIK has the same ABI but allows easier debugging

    // Same version that Vivecraft (at least for 1.16) uses
    inJar("net.java.dev.jna:jna:4.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1") // Use JUnit Jupiter API for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1") // Use JUnit Jupiter Engine for testing.

    // Also add PojavLauncher's GLFW stub for debugging
    // Note we need this in-jar so we can easily modify it without making pojav re-extract it
    inJarOnly(files("../jre_lwjgl3glfw/build/libs/jre_lwjgl3glfw-3.2.3.jar"))
    runtimeOnly(files(
            "../app_pojavlauncher/src/main/assets/components/lwjgl3/lwjgl.jar",
            "../app_pojavlauncher/src/main/assets/components/lwjgl3/lwjgl-opengl.jar",
            "../app_pojavlauncher/src/main/assets/components/lwjgl3/jsr305.jar"
    ))
}

application {
    // Define the main class for the application.
    mainClassName = "xyz.znix.graphicstest.Main"
}

val test by tasks.getting(Test::class) {
    // Use junit platform for unit tests
    useJUnitPlatform()
}

val jar = tasks.getByName("jar", Jar::class) {
    from((inJar + inJarOnly).map { zipTree(it) })
}

val fatJar = task("fatJar", Jar::class) {
    from(jar.source)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    archiveBaseName.set(jar.archiveBaseName.get() + "-fat")
    manifest.attributes["Main-Class"] = application.mainClassName
}
