package xyz.znix.graphicstest;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVRCompositor_FnTable;
import jopenvr.VR_IVRSystem_FnTable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;

public class OpenVR {
    public final VR_IVRSystem_FnTable system;
    public final VR_IVRCompositor_FnTable compositor;

    private final IntBuffer scratch = IntBuffer.allocate(1);

    public OpenVR() {
        // Set up OpenVR
        NativeLibrary.addSearchPath("openvr_api", ".");
        IntBuffer errBuf = IntBuffer.allocate(1);
        JOpenVRLibrary.VR_InitInternal(errBuf, JOpenVRLibrary.EVRApplicationType.EVRApplicationType_VRApplication_Scene);
        int err = errBuf.get(0);
        if (err != 0) {
            throw new RuntimeException("Failed to init openvr: " + err);
        }

        system = getInterface(JOpenVRLibrary.IVRSystem_Version, VR_IVRSystem_FnTable.class);
        compositor = getInterface(JOpenVRLibrary.IVRCompositor_Version, VR_IVRCompositor_FnTable.class);
    }

    private <T extends Structure> T getInterface(String name, Class<T> type) {
        scratch.put(0, -1);
        Pointer p = JOpenVRLibrary.VR_GetGenericInterface(name, scratch);

        if (scratch.get(0) != JOpenVRLibrary.EVRInitError.EVRInitError_VRInitError_None) {
            throw new RuntimeException("Could not get interface " + name + ": " + scratch.get(0));
        }

        T instance;
        try {
            Constructor<T> ctor = type.getConstructor(Pointer.class);
            instance = ctor.newInstance(p);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create interface wrapper for " + name, e);
        }

        instance.setAutoSynch(false);
        instance.read();

        return instance;
    }
}
