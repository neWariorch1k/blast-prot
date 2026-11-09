package io.github.blastprotectionhud.compat;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Thin wrapper over the GUI drawing API so the rest of the mod stays version independent. */
public final class Gfx {
    private static final Map<Identifier, Boolean> EXISTS = new HashMap<>();

    private final GuiGraphicsExtractor graphics;

    public Gfx(GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
    }

    public int width() {
        return graphics.guiWidth();
    }

    public int height() {
        return graphics.guiHeight();
    }

    public void text(Font font, String text, int x, int y, int argb) {
        graphics.text(font, text, x, y, argb, true);
    }

    public void centeredText(Font font, Component text, int x, int y, int argb) {
        graphics.centeredText(font, text, x, y, argb);
    }

    public void push(float x, float y, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
    }

    public void pop() {
        graphics.pose().popMatrix();
    }

    /** Draws a square texture; returns false (and draws nothing) if the texture does not exist. */
    public boolean texture(String namespace, String path, int x, int y, int size, int argb) {
        Identifier id = Identifier.tryBuild(namespace, path);
        if (id == null || !EXISTS.computeIfAbsent(id, Gfx::exists)) {
            return false;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, id, x, y, 0.0F, 0.0F, size, size, size, size, argb);
        return true;
    }

    private static Boolean exists(Identifier id) {
        return Minecraft.getInstance().getResourceManager().getResource(id).isPresent();
    }
}
