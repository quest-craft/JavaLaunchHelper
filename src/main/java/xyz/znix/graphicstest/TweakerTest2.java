package xyz.znix.graphicstest;

import com.sun.jna.NativeLibrary;

public class TweakerTest2 {
    public static void test(Object instance) {
        NativeLibrary lib = (NativeLibrary) instance;

        lib.getFunction("VR_GetGenericInterface");
    }
}
