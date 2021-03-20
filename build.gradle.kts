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

val inJar by configurations.creating

dependencies {
    val lwjglVersion = "2.9.3"

    implementation("org.lwjgl.lwjgl:lwjgl:$lwjglVersion")
    //runtimeOnly("org.lwjgl.lwjgl", "lwjgl", lwjglVersion, classifier = lwjglNatives)
    implementation("org.lwjgl.lwjgl:lwjgl_util:$lwjglVersion")

    // All inJar dependencies are also implementation dependencies
    implementation(inJar)

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
