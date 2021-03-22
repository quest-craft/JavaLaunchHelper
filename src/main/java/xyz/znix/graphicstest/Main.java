/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package xyz.znix.graphicstest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import jopenvr.*;
import jopenvr.JOpenVRLibrary.*;
import jopenvr.JOpenVRLibrary.ETextureType;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT;

public class Main {
    public static final boolean USE_VR = true;

    // The window handle
    private long window;

    private OpenVR vr;
    private EyeBuffer[] eyes;

    long frameCount;

    TrackedDevicePose_t pose;

    public void run() {
        System.out.println("Hello LWJGL " + Sys.getVersion() + "!");

        init();
        loop();
    }

    private void init() {
        if (USE_VR) {
            pose = new TrackedDevicePose_t();
        }

        try {
            Display.setTitle("Hello World");
            DisplayMode mode = null;
            for (DisplayMode m : Display.getAvailableDisplayModes()) {
                System.out.println("Mode: " + m);
                if (m.getWidth() == 480)
                    mode = m;
            }
            if (mode != null) {
                Display.setDisplayMode(mode);
            }
            Display.setFullscreen(false);
            Display.setResizable(true);
            Display.create();
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }

        // Log OpenGL errors
        // Not available on Android
        try {
            setupDebugCallback();
        } catch (NoClassDefFoundError err) {
            System.err.println("Could not setup OpenGL debug callbacks: " + err);
        }

        System.out.println("GL vendor: " + glGetString(GL_VENDOR));

        // Setup VR
        if (USE_VR)
            vr = new OpenVR();

        eyes = new EyeBuffer[]{new EyeBuffer(), new EyeBuffer()};
    }

    private void loop() {
        int i = 0;

        System.out.println("Display size: " + Display.getWidth() + " " + Display.getHeight());

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!Display.isCloseRequested()) {
            if (USE_VR)
                vr.compositor.WaitGetPoses.apply(pose, 1, null, 0);

            // Desktop view
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            drawScene(null, -1);

            for (EyeBuffer eye : eyes) {
                glBindFramebuffer(GL_FRAMEBUFFER, eye.frameBuffer);
                drawScene(eye, eye == eyes[0] ? 0 : 1);
            }

            if (!USE_VR) {
                // No need to sync if the VR compositor is doing that for us
                // Display.sync(60);
                try {
                    //noinspection BusyWait
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Submit the textures for both eyes to the runtime
                for (int eye = 0; eye < 2; eye++) {
                    Texture_t tex = new Texture_t();
                    tex.eType = ETextureType.ETextureType_TextureType_OpenGL;
                    tex.eColorSpace = EColorSpace.EColorSpace_ColorSpace_Gamma;
                    tex.handle = new Pointer(eyes[eye].tex);
                    tex.write();

                    vr.compositor.Submit.apply(eye, tex, null, 0);

                    // Not exactly my favourite way of handling this, but JNA doesn't let us dispose the memory ourselves
                    // Instead, it'll automatically do it when it's Java representation is GC'd
                }
            }

            Display.update();

            frameCount++;
        }

        Display.destroy();
    }

    private void drawScene(EyeBuffer eye, int eyeId) {
        // Set the clear colour
        glClearColor(0, (float) Math.sin((float) frameCount / 800) * 0.2f + 0.2f, 0.0f, 1.0f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        // Make up some usable aspect ratio if we have a 0x0 screen, as we do on android
        // Unless we can get the values passed in by the pojav launcher
        float aspect;
        int width = Integer.getInteger("glfwstub.windowWidth", Display.getWidth());
        int height = Integer.getInteger("glfwstub.windowHeight", Display.getHeight());

        if (eye != null) {
            width = eye.width;
            height = eye.height;
        }

        if (width == 0) {
            aspect = 1;
        } else {
            aspect = 1.0f * width / height;
        }

        glViewport(0, 0, width, height);

        glLoadIdentity();
        // TODO vary for VR viewports
        if (eye == null || !USE_VR) {
            GLU.gluPerspective(90, aspect, 0.05f, 100);
        } else {
            HmdMatrix44_t.ByValue mat = vr.system.GetProjectionMatrix.apply(eyeId, 0.05f, 100);
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            // Transpose it
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    int i = x * 4 + y;
                    int ti = x + y * 4;
                    fb.put(i, mat.m[ti]);
                }
            }

            glMultMatrix(fb);
        }
        // GLU.gluOrtho2D(-1, -1, 1, 1);

        if (USE_VR && eye != null) {
            pose.read();

            transformInverseBy(vr.system.GetEyeToHeadTransform.apply(eyeId));
            transformInverseBy(pose.mDeviceToAbsoluteTracking);
        }

        glTranslatef(0, 0, -2);

        // Spin over time
        glRotatef(frameCount * 0.8f, 0, 1, 0);
        glRotatef(frameCount * 0.1f, 1, 0, 0);

        // Translate in by half a unit so the cube is centred
        glTranslatef(-0.5f, -0.5f, -0.5f);

        glBegin(GL_QUADS);

        // front
        glColor3f(1, 0, 0);
        glVertex3f(0, 0, 1);
        glVertex3f(1, 0, 1);
        glVertex3f(1, 1, 1);
        glVertex3f(0, 1, 1);
        // back
        glColor3f(0, 1, 0);
        glVertex3f(0, 0, 0);
        glVertex3f(1, 0, 0);
        glVertex3f(1, 1, 0);
        glVertex3f(0, 1, 0);
        // top
        glColor3f(0, 0, 1);
        glVertex3f(0, 1, 0);
        glVertex3f(1, 1, 0);
        glVertex3f(1, 1, 1);
        glVertex3f(0, 1, 1);
        // bottom
        glColor3f(1, 0, 1);
        glVertex3f(0, 0, 0);
        glVertex3f(1, 0, 0);
        glVertex3f(1, 0, 1);
        glVertex3f(0, 0, 1);
        // left
        glColor3f(0, 1, 1);
        glVertex3f(0, 0, 0);
        glVertex3f(0, 1, 0);
        glVertex3f(0, 1, 1);
        glVertex3f(0, 0, 1);
        // right
        glColor3f(1, 1, 1);
        glVertex3f(1, 0, 0);
        glVertex3f(1, 1, 0);
        glVertex3f(1, 1, 1);
        glVertex3f(1, 0, 1);

        glEnd();

        // HACK around glfinish rearranging our draws and clears
        glFinish();
    }

    public static void main(String[] args) {
        System.out.println("hello");

        Thread thr = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("stacktrace timer up, logging:");
            for (Map.Entry<Thread, StackTraceElement[]> item : Thread.getAllStackTraces().entrySet()) {
                System.out.println("Thread: " + item.getKey().getName());
                for (StackTraceElement st : item.getValue()) {
                    System.out.println(st);
                }
            }
        });
        //thr.start();

        Main main = new Main();
        main.run();
    }

    private class EyeBuffer {
        int tex;
        int frameBuffer;

        int width, height;

        EyeBuffer() {
            int err = glGetError();
            if (err != 0) {
                throw new RuntimeException("OpenGL pre-failed: " + err);
            }

            // Create the texture
            IntByReference widthRef = new IntByReference();
            IntByReference heightRef = new IntByReference();
            if (USE_VR) {
                vr.system.GetRecommendedRenderTargetSize.apply(widthRef, heightRef);
            } else {
                widthRef.setValue(500);
                heightRef.setValue(600);
            }

            width = widthRef.getValue();
            height = heightRef.getValue();

            tex = glGenTextures();

            err = glGetError();
            if (err != 0) {
                throw new RuntimeException("OpenGL Failed creating texture: " + err);
            }

            if (tex == 0) {
                throw new RuntimeException("glGenTextures returned 0");
            }

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, tex);
            // glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width.getValue(), height.getValue());

            // This line is copied (almost) directly from vivecraft
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);

            // This emulates the behaviour of gl4es:
            // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.getValue(), height.getValue(), 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);

            err = glGetError();
            if (err != 0) {
                throw new RuntimeException("OpenGL Failed setting texture contents: " + err);
            }

            // Create the frame buffer
            frameBuffer = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

            glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, tex, 0);

            // The depth buffer
            int depthRenderBuffer = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBuffer);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBuffer);

            // Last error check
            err = glGetError();
            if (err != 0) {
                throw new RuntimeException("OpenGL Failed FB: " + err);
            }

            int format = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT);
            System.out.println("Created eye tex: name=" + tex + " fmt=" + format);
        }
    }

    private void setupDebugCallback() {
        GL43.glDebugMessageCallback(new KHRDebugCallback());
        glEnable(GL_DEBUG_OUTPUT);
        // if ((glGetInteger(GL_CONTEXT_FLAGS) & KHRDebug.GL_CONTEXT_FLAG_DEBUG_BIT) == 0) {
        //     System.out.println("[GL] Warning: A non-debug context may not produce any debug output.");
        // }
    }

    private void transformInverseBy(HmdMatrix34_t mat) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        // Don't transpose it - OpenVR uses row-major while OpenGL uses column-major, but since this is
        // the device-to-origin pose we want to invert that - if you lean back, the cube should move forwads
        // relative to your head, not backwards.
        // Thus we have to rotate, then translate by the negative head position.
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int i = x * 4 + y;
                int ti = x * 4 + y;
                fb.put(i, mat.m[ti]);
            }
        }
        fb.put(3 * 4 + 3, 1);
        glMultMatrix(fb);

        //noinspection PointlessArithmeticExpression
        glTranslatef(-mat.m[4 * 0 + 3], -mat.m[4 * 1 + 3], -mat.m[4 * 2 + 3]);
    }
}
