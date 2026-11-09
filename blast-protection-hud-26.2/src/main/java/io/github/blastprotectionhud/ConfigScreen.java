package io.github.blastprotectionhud;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.blastprotectionhud.HudConfig.Anchor;
import io.github.blastprotectionhud.HudConfig.IconStyle;
import io.github.blastprotectionhud.compat.BaseScreen;
import io.github.blastprotectionhud.compat.Compat;
import io.github.blastprotectionhud.compat.Gfx;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ConfigScreen extends BaseScreen {
    private static final String KEY = "options.blastprotectionhud.";
    private static final int BUTTON_WIDTH = 150;
    private static final int ROW = 22;

    private final Screen parent;
    private final HudConfig config;
    private IntSlider offsetX;
    private IntSlider offsetY;
    private Button toggleKeyButton;
    private Button settingsKeyButton;
    private KeyMapping listening;

    public ConfigScreen(Screen parent) {
        super(Component.translatable(KEY + "title"));
        this.config = HudConfig.get();
        this.parent = parent;
    }

    private static Component enumLabel(String prefix, Enum<?> value) {
        return Component.translatable(KEY + prefix + "." + value.name().toLowerCase(Locale.ROOT));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - BUTTON_WIDTH - 5;
        int right = this.width / 2 + 5;
        int full = 310;
        int top = 26;

        addRenderableWidget(CycleButton.onOffBuilder(config.enabled)
                .create(left, top, BUTTON_WIDTH, 20, Component.translatable(KEY + "enabled"),
                        (button, value) -> config.enabled = value));

        addRenderableWidget(Compat.<IconStyle>cycleButton(style -> enumLabel("icon", style), IconStyle.values(), config.icon)
                .create(right, top, BUTTON_WIDTH, 20, Component.translatable(KEY + "icon"),
                        (button, value) -> config.icon = value));

        addRenderableWidget(CycleButton.onOffBuilder(config.showWhenMissing)
                .create(left, top + ROW, BUTTON_WIDTH, 20, Component.translatable(KEY + "show_missing"),
                        (button, value) -> config.showWhenMissing = value));

        addRenderableWidget(CycleButton.onOffBuilder(config.showLevel)
                .create(right, top + ROW, BUTTON_WIDTH, 20, Component.translatable(KEY + "show_level"),
                        (button, value) -> config.showLevel = value));

        addRenderableWidget(new IntSlider(left, top + ROW * 2, 10, 100, config.opacity,
                v -> Component.translatable(KEY + "opacity", v + "%"),
                v -> config.opacity = v));

        addRenderableWidget(new IntSlider(right, top + ROW * 2, 50, 300, config.scale,
                v -> Component.translatable(KEY + "scale", v + "%"),
                v -> config.scale = v));

        addRenderableWidget(Compat.<Anchor>cycleButton(anchor -> enumLabel("anchor", anchor), Anchor.values(), config.anchor)
                .create(left, top + ROW * 3, full, 20, Component.translatable(KEY + "anchor"),
                        (button, value) -> config.anchor = value));

        offsetX = addRenderableWidget(new IntSlider(left, top + ROW * 4, -300, 300, config.offsetX,
                v -> Component.translatable(KEY + "offset_x", v),
                v -> config.offsetX = v));

        offsetY = addRenderableWidget(new IntSlider(right, top + ROW * 4, -300, 300, config.offsetY,
                v -> Component.translatable(KEY + "offset_y", v),
                v -> config.offsetY = v));

        addRenderableWidget(Compat.<HudConfig.AlertSound>cycleButton(sound -> enumLabel("sound", sound),
                        HudConfig.AlertSound.values(), config.selfSound)
                .create(left, top + ROW * 5, BUTTON_WIDTH, 20, Component.translatable(KEY + "self_sound"),
                        (button, value) -> {
                            config.selfSound = value;
                            AlertSounds.play(value, config.soundVolume);
                        }));

        addRenderableWidget(Compat.<HudConfig.AlertSound>cycleButton(sound -> enumLabel("sound", sound),
                        HudConfig.AlertSound.values(), config.enemySound)
                .create(right, top + ROW * 5, BUTTON_WIDTH, 20, Component.translatable(KEY + "enemy_sound"),
                        (button, value) -> {
                            config.enemySound = value;
                            AlertSounds.play(value, config.soundVolume);
                        }));

        addRenderableWidget(new IntSlider(left, top + ROW * 6, 0, 100, config.soundVolume,
                v -> Component.translatable(KEY + "volume", v + "%"),
                v -> config.soundVolume = v));

        addRenderableWidget(Button.builder(Component.translatable(KEY + "reset_offset"), button -> {
                    offsetX.set(0);
                    offsetY.set(0);
                })
                .bounds(right, top + ROW * 6, BUTTON_WIDTH, 20)
                .build());

        toggleKeyButton = addRenderableWidget(Button.builder(Component.empty(),
                        button -> listenFor(BlastProtectionHud.toggleKey))
                .bounds(left, top + ROW * 7, BUTTON_WIDTH, 20)
                .build());

        settingsKeyButton = addRenderableWidget(Button.builder(Component.empty(),
                        button -> listenFor(BlastProtectionHud.settingsKey))
                .bounds(right, top + ROW * 7, BUTTON_WIDTH, 20)
                .build());

        updateKeyButtons();

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(left, top + ROW * 8, full, 20)
                .build());
    }

    private void listenFor(KeyMapping mapping) {
        listening = mapping;
        updateKeyButtons();
    }

    private void updateKeyButtons() {
        toggleKeyButton.setMessage(Component.translatable(KEY + "key_toggle", keyLabel(BlastProtectionHud.toggleKey)));
        settingsKeyButton.setMessage(Component.translatable(KEY + "key_settings", keyLabel(BlastProtectionHud.settingsKey)));
        Tooltip hint = listening == null ? null : Tooltip.create(Component.translatable(KEY + "key_hint"));
        toggleKeyButton.setTooltip(listening == BlastProtectionHud.toggleKey ? hint : null);
        settingsKeyButton.setTooltip(listening == BlastProtectionHud.settingsKey ? hint : null);
    }

    private Component keyLabel(KeyMapping mapping) {
        if (mapping == listening) {
            return Component.literal("> ")
                    .append(mapping.getTranslatedKeyMessage())
                    .append(" <")
                    .withStyle(ChatFormatting.YELLOW);
        }
        return mapping.getTranslatedKeyMessage();
    }

    @Override
    protected boolean onKey(int key) {
        if (listening == null) {
            return false;
        }
        listening.setKey(key == Compat.escapeKey() ? InputConstants.UNKNOWN : Compat.keyboardKey(key));
        KeyMapping.resetMapping();
        if (minecraft != null) {
            minecraft.options.save();
        }
        listening = null;
        updateKeyButtons();
        return true;
    }

    @Override
    protected void drawUnderlay(Gfx gfx) {
        IndicatorRenderer.Status status = minecraft != null && minecraft.player != null
                ? IndicatorRenderer.Status.of(minecraft.player)
                : IndicatorRenderer.Status.PREVIEW;
        if (status.level() <= 0 && !config.showWhenMissing) {
            status = IndicatorRenderer.Status.PREVIEW;
        }
        IndicatorRenderer.draw(gfx, font, config, status);
    }

    @Override
    protected void drawOverlay(Gfx gfx) {
        gfx.centeredText(font, title, width / 2, 10, -1);
    }

    @Override
    public void onClose() {
        config.save();
        if (minecraft != null) {
            Compat.setScreen(minecraft, parent);
        }
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final IntFunction<Component> label;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int min, int max, int initial, IntFunction<Component> label, IntConsumer onChange) {
            super(x, y, BUTTON_WIDTH, 20, Component.empty(), (double) (initial - min) / (max - min));
            this.min = min;
            this.max = max;
            this.label = label;
            this.onChange = onChange;
            updateMessage();
        }

        private int intValue() {
            return min + (int) Math.round(value * (max - min));
        }

        void set(int newValue) {
            this.value = (double) (newValue - min) / (max - min);
            applyValue();
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(label.apply(intValue()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(intValue());
        }
    }
}
