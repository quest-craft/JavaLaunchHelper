package xyz.znix.graphicstest;

import org.lwjgl.Sys;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TweakTester {
    public static void main(String... args) throws IOException, ReflectiveOperationException {
        // testJnaTweak();
        testVivecraftTweak();
    }

    private static void testJnaTweak() throws IOException, ReflectiveOperationException {
        JarFile jf = new JarFile("jna-4.2.1.jar");

        ZipEntry entry = jf.getEntry("com/sun/jna/NativeLibrary.class");
        InputStream is = jf.getInputStream(entry);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte buf[] = new byte[40960];
        while (true) {
            int count = is.read(buf);
            if (count == -1)
                break;
            bos.write(buf, 0, count);
        }

        byte[] data = bos.toByteArray();
        writeZip("/tmp/jna-old.jar", data);

        QuestcraftTransformer transformer = new QuestcraftTransformer();
        byte[] modified = transformer.transform("com.sun.jna.NativeLibrary", null, data);

        writeZip("/tmp/jna-new.jar", modified);

        // Try it out
        // ByteArrayClassLoader bacl = new ByteArrayClassLoader();
        // bacl.name = "com.sun.jna.NativeLibrary";
        // bacl.bytes = modified;
        // Class<?> cl = bacl.loadClass(bacl.name);

        ClassLoader loader = TweakTester.class.getClassLoader();
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        Class<?> cl = (Class<?>) defineClass.invoke(loader, "com.sun.jna.NativeLibrary", modified, 0, modified.length);

        Method m = cl.getDeclaredMethod("getInstance", String.class, Map.class);
        Object instance = m.invoke(null, "libopenvr_api.so", Collections.emptyMap());
        TweakerTest2.test(instance);
    }

    private static void testVivecraftTweak() throws IOException, ReflectiveOperationException {
        JarFile jf = new JarFile("minecrift-1.12.2-jrbudda-11-r2.jar");

        ZipEntry entry = jf.getEntry("org/vivecraft/utils/InputInjector.class");
        InputStream is = jf.getInputStream(entry);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte buf[] = new byte[40960];
        while (true) {
            int count = is.read(buf);
            if (count == -1)
                break;
            bos.write(buf, 0, count);
        }

        byte[] data = bos.toByteArray();
        writeZip("/tmp/jna-old.jar", data);

        QuestcraftTransformer transformer = new QuestcraftTransformer();
        byte[] modified = transformer.transform("org.vivecraft.utils.InputInjector", null, data);

        writeZip("/tmp/jna-new.jar", modified);

        // Try it out
        // ByteArrayClassLoader bacl = new ByteArrayClassLoader();
        // bacl.name = "com.sun.jna.NativeLibrary";
        // bacl.bytes = modified;
        // Class<?> cl = bacl.loadClass(bacl.name);

        // ClassLoader loader = TweakTester.class.getClassLoader();
        // Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        // defineClass.setAccessible(true);
        // Class<?> cl = (Class<?>) defineClass.invoke(loader, "com.sun.jna.NativeLibrary", modified, 0, modified.length);

        // Method m = cl.getDeclaredMethod("getInstance", String.class, Map.class);
        // Object instance = m.invoke(null, "libopenvr_api.so", Collections.emptyMap());
        // TweakerTest2.test(instance);
    }

    private static void writeZip(String path, byte[] data) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));
        ZipEntry e = new ZipEntry("com/sun/jna/NativeLibrary.class");
        out.putNextEntry(e);

        out.write(data);
        out.closeEntry();

        out.close();
    }

    // public static class ByteArrayClassLoader extends ClassLoader {
    //     String name;
    //     byte[] bytes;
    //     ClassLoader realSuper = getClass().getClassLoader();

    //     ByteArrayClassLoader() {
    //         super(null);
    //     }

    //     @Override
    //     public Class<?> findClass(String name) throws ClassNotFoundException {
    //         if (!this.name.equals(name))
    //             return realSuper.loadClass(name);

    //         return defineClass(name, bytes, 0, bytes.length);
    //     }
    // }
}
