# Graphics test app

Sample application to test custom graphics stuff in. This is a regular LWJGL application that runs inside OpenJDK, like
minecraft would.

## Running on your development machine

Since the point of this application is to test all the APIs and basically act similar to Minecraft and Vivecraft, you can
run this natively on your development machine.

Either run it as a regular gradle project in your IDE, or use:

```
./gradlew run
```

## Running on the device

Run gradle in continuous mode to compile the JAR. This means whenever you modify your source code, it re-builds the
destination JAR file.

```
./gradlew --continuous jar
```

With that running, also run a simple HTTP server in `build/libs`:

```
python3 -m http.server 8001
```

This is required, since the app will download and run this jar when you hit run on the app. This means you can edit
your source-code, then hit run without having to fiddle around copying stuff.

Edit RenderingTestRunActivity in the app to hard-code your IP address instead of mine. Yes it's horrible doing stuff
like that, no I don't care - this is only for initial testing stuff.

Next launch the RenderingTestRunActivity in the custom PojavLauncher build. This will have a 'START JVM' full-screen
button. Clicking this will download and run the JAR.

To view the output of the Java process running everything, use:

```
adb shell cat /storage/emulated/0/games/PojavLauncher/logout
```

(both stdout and stderr are redirected there)
