package io.github.blastprotectionhud;

import io.github.blastprotectionhud.compat.Compat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlastProtectionHud implements ClientModInitializer {
    public static final String MOD_ID = "blastprotectionhud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyMapping toggleKey;
    public static KeyMapping settingsKey;

    @Override
    public void onInitializeClient() {
        HudConfig.get();

        toggleKey = Compat.registerKey("key.blastprotectionhud.toggle");
        settingsKey = Compat.registerKey("key.blastprotectionhud.settings");

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            AlertSounds.tick(minecraft);

            while (toggleKey.consumeClick()) {
                HudConfig config = HudConfig.get();
                config.enabled = !config.enabled;
                config.save();
                if (minecraft.player != null) {
                    Compat.actionBar(minecraft.player, Component.translatable(
                            config.enabled ? "message.blastprotectionhud.enabled" : "message.blastprotectionhud.disabled"));
                }
            }

            while (settingsKey.consumeClick()) {
                if (Compat.screen(minecraft) == null) {
                    Compat.setScreen(minecraft, new ConfigScreen(null));
                }
            }
        });

        Compat.registerHud(IndicatorRenderer::renderHud);
    }
}
