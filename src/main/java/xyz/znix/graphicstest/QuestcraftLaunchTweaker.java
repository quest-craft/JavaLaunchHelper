package xyz.znix.graphicstest;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

public class QuestcraftLaunchTweaker implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.registerTransformer(QuestcraftTransformer.class.getName());
    }

    @Override
    public String getLaunchTarget() {
        throw new UnsupportedOperationException("Cannot use Questcraft launch tweaker as primary");
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
