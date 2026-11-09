package io.github.blastprotectionhud;

import io.github.blastprotectionhud.compat.Compat;
import io.github.blastprotectionhud.compat.Gfx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class IndicatorRenderer {
    private static final int ICON = 16;
    private static final int CROSSHAIR_GAP = 12;
    private static final int EDGE_MARGIN = 4;
    private static final int MISSING_COLOR = 0xFF5050;
    private static final int LEVEL_COLOR = 0xFFE070;
    private static final String MISSING_MARK = "✗";

    private IndicatorRenderer() {
    }

    public record Status(int level) {
        public static final Status PREVIEW = new Status(4);

        public static Status of(Player player) {
            return new Status(blastProtectionLevel(player, player.getItemBySlot(EquipmentSlot.LEGS)));
        }
    }

    public static int blastProtectionLevel(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return player.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.BLAST_PROTECTION)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    public static void renderHud(Gfx gfx) {
        Minecraft minecraft = Minecraft.getInstance();
        HudConfig config = HudConfig.get();
        if (!config.enabled || Compat.isHudHidden(minecraft) || minecraft.player == null || minecraft.player.isSpectator()) {
            return;
        }
        Status status = Status.of(minecraft.player);
        if (status.level() <= 0 && !config.showWhenMissing) {
            return;
        }
        draw(gfx, minecraft.font, config, status);
    }

    public static void draw(Gfx gfx, Font font, HudConfig config, Status status) {
        boolean worn = status.level() > 0;
        String roman = config.showLevel && worn ? toRoman(status.level()) : "";
        int contentWidth = ICON + (roman.isEmpty() ? 0 : 1 + font.width(roman));
        float scale = config.scale / 100.0F;
        float width = contentWidth * scale;
        float height = ICON * scale;
        float centerX = gfx.width() / 2.0F;
        float centerY = gfx.height() / 2.0F;

        float x;
        float y;
        switch (config.anchor) {
            case CROSSHAIR_LEFT -> {
                x = centerX - CROSSHAIR_GAP - width;
                y = centerY - height / 2.0F;
            }
            case CROSSHAIR_RIGHT -> {
                x = centerX + CROSSHAIR_GAP;
                y = centerY - height / 2.0F;
            }
            case CROSSHAIR_ABOVE -> {
                x = centerX - width / 2.0F;
                y = centerY - CROSSHAIR_GAP - height;
            }
            case CROSSHAIR_BELOW -> {
                x = centerX - width / 2.0F;
                y = centerY + CROSSHAIR_GAP;
            }
            case TOP_LEFT -> {
                x = EDGE_MARGIN;
                y = EDGE_MARGIN;
            }
            case TOP_RIGHT -> {
                x = gfx.width() - EDGE_MARGIN - width;
                y = EDGE_MARGIN;
            }
            case BOTTOM_LEFT -> {
                x = EDGE_MARGIN;
                y = gfx.height() - EDGE_MARGIN - height;
            }
            default -> {
                x = gfx.width() - EDGE_MARGIN - width;
                y = gfx.height() - EDGE_MARGIN - height;
            }
        }
        x += config.offsetX;
        y += config.offsetY;

        int alpha = Math.round(config.opacity * 2.55F);

        gfx.push(x, y, scale);
        gfx.texture(BlastProtectionHud.MOD_ID, config.icon.texture(), 0, 0, ICON,
                (worn ? alpha : alpha / 3) << 24 | 0xFFFFFF);
        if (worn) {
            if (!roman.isEmpty()) {
                gfx.text(font, roman, ICON + 1, (ICON - font.lineHeight) / 2 + 1, alpha << 24 | LEVEL_COLOR);
            }
        } else {
            gfx.text(font, MISSING_MARK, (ICON - font.width(MISSING_MARK)) / 2, (ICON - font.lineHeight) / 2 + 1,
                    alpha << 24 | MISSING_COLOR);
        }
        gfx.pop();
    }

    private static String toRoman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(level);
        };
    }
}
