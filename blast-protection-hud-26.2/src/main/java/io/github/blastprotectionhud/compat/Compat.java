package io.github.blastprotectionhud.compat;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Everything that differs between Minecraft versions lives in this package.
 * This copy targets Minecraft 26.2.
 */
public final class Compat {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("blastprotectionhud", "main"));

    private Compat() {
    }

    public static void registerHud(Consumer<Gfx> renderer) {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("blastprotectionhud", "indicator"),
                (extractor, tracker) -> renderer.accept(new Gfx(extractor)));
    }

    public static KeyMapping registerKey(String translationKey) {
        return KeyMappingHelper.registerKeyMapping(
                new KeyMapping(translationKey, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY));
    }

    /** Key code as delivered by {@code KeyEvent#key()} (GLFW key codes in 26.2). */
    public static InputConstants.Key keyboardKey(int key) {
        return InputConstants.Type.KEYBOARD.getOrCreate(key);
    }

    /** Key code of the Escape key as delivered by {@code KeyEvent#key()}. */
    public static int escapeKey() {
        return InputConstants.KEY_ESCAPE;
    }

    public static <T> CycleButton.Builder<T> cycleButton(Function<T, Component> label, T[] values, T initial) {
        return CycleButton.builder(label, initial).withValues(values);
    }

    public static Screen screen(Minecraft minecraft) {
        return minecraft.gui.screen();
    }

    public static void setScreen(Minecraft minecraft, Screen screen) {
        minecraft.gui.setScreen(screen);
    }

    public static boolean isHudHidden(Minecraft minecraft) {
        return minecraft.gui.hud.isHidden();
    }

    /** Shows a short message above the hotbar. */
    public static void actionBar(LocalPlayer player, Component message) {
        Minecraft.getInstance().gui.hud.setOverlayMessage(message, false);
    }
}
