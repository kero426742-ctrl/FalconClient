package com.falcon;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** On/off settings, saved to config/falconclient.properties. */
public final class FalconConfig {
    public static boolean showCps = true;
    public static boolean showFps = true;
    public static boolean showPing = true;
    public static boolean showPlaytime = true;
    public static boolean showTotems = true;
    public static boolean showArmor = true;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("falconclient.properties");
    }

    private static boolean get(Properties p, String key) {
        return Boolean.parseBoolean(p.getProperty(key, "true"));
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        showCps = get(p, "showCps");
        showFps = get(p, "showFps");
        showPing = get(p, "showPing");
        showPlaytime = get(p, "showPlaytime");
        showTotems = get(p, "showTotems");
        showArmor = get(p, "showArmor");
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("showCps", Boolean.toString(showCps));
        p.setProperty("showFps", Boolean.toString(showFps));
        p.setProperty("showPing", Boolean.toString(showPing));
        p.setProperty("showPlaytime", Boolean.toString(showPlaytime));
        p.setProperty("showTotems", Boolean.toString(showTotems));
        p.setProperty("showArmor", Boolean.toString(showArmor));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "FalconClient settings");
        } catch (IOException ignored) {
        }
    }

    private FalconConfig() {}
}
