package io.github.blastprotectionhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;

public final class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("blastprotectionhud.json");
    private static HudConfig instance;

    public enum IconStyle {
        CLASSIC, EXCLAMATION, PANTS, BURST;

        public String texture() {
            return "textures/gui/" + name().toLowerCase(Locale.ROOT) + ".png";
        }
    }

    public enum Anchor {
        CROSSHAIR_LEFT, CROSSHAIR_RIGHT, CROSSHAIR_ABOVE, CROSSHAIR_BELOW,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public enum AlertSound {
        OFF, ORB, PLING, BELL, CHIME, BASS, ANVIL, LEVEL_UP
    }

    public boolean enabled = true;
    public IconStyle icon = IconStyle.CLASSIC;
    public boolean showWhenMissing = false;
    public boolean showLevel = false;
    public int opacity = 100;
    public int scale = 100;
    public Anchor anchor = Anchor.CROSSHAIR_BELOW;
    public int offsetX = 0;
    public int offsetY = 0;
    public AlertSound selfSound = AlertSound.ORB;
    public AlertSound enemySound = AlertSound.BELL;
    public int soundVolume = 100;

    public static HudConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static HudConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
                if (loaded != null) {
                    return loaded.sanitize();
                }
            } catch (Exception e) {
                BlastProtectionHud.LOGGER.warn("Failed to read {}, using defaults", PATH, e);
            }
        }
        return new HudConfig();
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            BlastProtectionHud.LOGGER.warn("Failed to save {}", PATH, e);
        }
    }

    private HudConfig sanitize() {
        if (icon == null) {
            icon = IconStyle.CLASSIC;
        }
        if (anchor == null) {
            anchor = Anchor.CROSSHAIR_BELOW;
        }
        if (selfSound == null) {
            selfSound = AlertSound.ORB;
        }
        if (enemySound == null) {
            enemySound = AlertSound.BELL;
        }
        soundVolume = Math.clamp(soundVolume, 0, 100);
        opacity = Math.clamp(opacity, 10, 100);
        scale = Math.clamp(scale, 50, 300);
        offsetX = Math.clamp(offsetX, -300, 300);
        offsetY = Math.clamp(offsetY, -300, 300);
        return this;
    }
}
