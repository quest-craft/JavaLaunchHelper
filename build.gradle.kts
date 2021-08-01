import org.gradle.internal.os.OperatingSystem
import java.net.URI
import java.util.Properties

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

var pojavDir = ".." // Set this as required
val componentsDir = "$pojavDir/app_pojavlauncher/src/main/assets/components"

// If we're build as part of PojavLauncher, one of the dependency files must be compiled first
if (rootProject.name == "PojavLauncher") {
    pojavDir = "../.."
    tasks.getByName("compileJava") {
        dependsOn(":jre_lwjgl3glfw:jar")
    }
}

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
    inJarOnly(files("$pojavDir/jre_lwjgl3glfw/build/libs/jre_lwjgl3glfw-3.2.3.jar"))
    runtimeOnly(files(
            "$componentsDir/lwjgl3/lwjgl.jar",
            "$componentsDir/lwjgl3/lwjgl-opengl.jar",
            "$componentsDir/lwjgl3/jsr305.jar"
    ))
}

application {
    // Define the main class for the application.
    mainClassName = "xyz.znix.graphicstest.Main"
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())
val java8Home = properties.getProperty("java8-home")
        ?: error("In local.properties, java8-home is not set")

// Compile with Java 8, since that's what's running on the device
val compileJava: JavaCompile by tasks
compileJava.options.isFork = true
compileJava.options.forkOptions.executable = "$java8Home/bin/javac"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
