package xyz.znix.graphicstest;

/*
import org.lwjgl.glfw.GLFW;

// From http://forum.lwjgl.org/index.php?topic=6057.msg32446#msg32446
public class SyncTimer {

    final static double
            NANO_RESOLUTION = 1000000000.0D,
            GLFW_RESOLUTION = 1.0D;

    public final static int
            JAVA_NANO = 1,
            LWJGL_GLFW = 2;

    private int mode;
    private double timeThen;
    private boolean enabled = true;

    public SyncTimer(int mode) {
        setNewMode(mode);
    }

    private double getResolution() {
        switch (mode) {
            case JAVA_NANO:
                return NANO_RESOLUTION;
            case LWJGL_GLFW:
                return GLFW_RESOLUTION;
        }
        return 0;
    }

    private double getTime() {
        switch (mode) {
            case JAVA_NANO:
                return System.nanoTime();
            case LWJGL_GLFW:
                return GLFW.glfwGetTime();
        }
        return 0;
    }


    public void setEnabled(boolean enable) {
        enabled = enable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setNewMode(int timerMode) {
        mode = timerMode;
        timeThen = getTime();
        System.out.println("Timer mode set to " + getModeString() + " timer");
    }

    public String getModeString() {
        switch (mode) {
            case JAVA_NANO:
                return "NANO";
            case LWJGL_GLFW:
                return "LWJGL";
        }
        return null;
    }

    public int getMode() {
        return mode;
    }

    public int sync(double fps) {
        try {
            return sync0(fps);
        } catch (Exception ex) {
            throw new RuntimeException("Exception in sync", ex);
        }
    }

    private int sync0(double fps) throws Exception {
        double resolution = getResolution();
        double timeNow = getTime();
        int updates = 0;

        if (enabled) {
            double gapTo = resolution / fps + timeThen;

            while (gapTo < timeNow) {
                gapTo = resolution / fps + gapTo;
                updates++;
            }
            while (gapTo > timeNow) {
                Thread.sleep(1);
                timeNow = getTime();
            }
            updates++;

            timeThen = gapTo;
        } else {
            while (timeThen < timeNow) {
                timeThen = resolution / fps + timeThen;
                updates++;
            }
        }

        return updates;
    }
}
 */
