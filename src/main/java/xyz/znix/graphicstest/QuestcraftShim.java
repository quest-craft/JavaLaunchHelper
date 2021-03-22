package xyz.znix.graphicstest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class QuestcraftShim {
    /**
     * Pass in the class to avoid some really annoying errors from trying to load it from our classloader.
     */
    public static Object hookGetInstance(String name, Class<?> cls) {
        if (name == null)
            return null;

        if (!name.equals("openvr_api"))
            return null;

        System.out.println("Skipping JNA Load of openvr_api and handling it ourselves");

        // Try loading by only the filename and let the system search for it
        // This is unlike normal JNA which won't do this - it thinks it knows where the library is, but
        // it's actually found Vivecraft's x86 library. We need our custom one with OpenComposite linked
        // into it, and the system can find that.

        try {
            int flags = -1; // DEFAULT_OPEN_OPTIONS

            Class<?> nativeCls = cls.getClassLoader().loadClass("com.sun.jna.Native");
            Method openMethod = nativeCls.getDeclaredMethod("open", String.class, Integer.TYPE);
            openMethod.setAccessible(true);
            long handle = (long) openMethod.invoke(null, "libopenvr_api.so", flags);

            Constructor<?> ctor = cls.getDeclaredConstructor(String.class, String.class, Long.TYPE, Map.class);
            ctor.setAccessible(true);
            // Note this special name is used in the getProcAddress hook
            return ctor.newInstance("<openvr-process>", null, handle, Collections.emptyMap());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Oops, error creating NativeLibrary", e);
        }
    }
}
