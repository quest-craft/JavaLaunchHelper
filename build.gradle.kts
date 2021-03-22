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

dependencies {
    val lwjglVersion = "2.9.3"

    implementation("org.lwjgl.lwjgl:lwjgl:$lwjglVersion")
    //runtimeOnly("org.lwjgl.lwjgl", "lwjgl", lwjglVersion, classifier = lwjglNatives)
    implementation("org.lwjgl.lwjgl:lwjgl_util:$lwjglVersion")

    // All inJar dependencies are also implementation dependencies
    implementation(inJar)

    // The Minecraft launchwrapper which is available at runtime
    // Implementation is fine for these, since only inJar files are emitted when sent to the device
    implementation("net.minecraft:launchwrapper:1.12")
    implementation("org.ow2.asm:asm-debug-all:5.2") // AFAIK has the same ABI but allows easier debugging

    // Same version that Vivecraft (at least for 1.16) uses
    inJar("net.java.dev.jna:jna:4.4.0")

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

val jar = tasks.getByName("jar", Jar::class) {
    from(inJar.map { zipTree(it) })
}

val fatJar = task("fatJar", Jar::class) {
    from(jar.source)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    archiveBaseName.set(jar.archiveBaseName.get() + "-fat")
    manifest.attributes["Main-Class"] = application.mainClassName
}
