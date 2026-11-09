package io.github.blastprotectionhud;

import io.github.blastprotectionhud.HudConfig.AlertSound;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;

/** Plays an alert when you (or an enemy player) put on Blast Protection leggings. */
public final class AlertSounds {
    private static ClientLevel trackedLevel;
    private static Boolean selfWearing;
    private static final Map<UUID, Boolean> othersWearing = new HashMap<>();

    private AlertSounds() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level != trackedLevel || minecraft.player == null) {
            trackedLevel = minecraft.level;
            selfWearing = null;
            othersWearing.clear();
        }
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        HudConfig config = HudConfig.get();

        boolean wearing = IndicatorRenderer.Status.of(minecraft.player).level() > 0;
        boolean selfPutOn = selfWearing != null && wearing && !selfWearing;
        selfWearing = wearing;

        boolean enemyPutOn = false;
        Set<UUID> seen = new HashSet<>();
        for (AbstractClientPlayer other : minecraft.level.players()) {
            if (other == minecraft.player || other.isSpectator() || other.isAlliedTo(minecraft.player)) {
                continue;
            }
            UUID id = other.getUUID();
            seen.add(id);
            boolean otherWearing = IndicatorRenderer.blastProtectionLevel(other, other.getItemBySlot(EquipmentSlot.LEGS)) > 0;
            Boolean previous = othersWearing.put(id, otherWearing);
            if (previous != null && otherWearing && !previous) {
                enemyPutOn = true;
            }
        }
        othersWearing.keySet().retainAll(seen);

        if (!config.enabled) {
            return;
        }
        if (selfPutOn) {
            play(config.selfSound, config.soundVolume);
        }
        if (enemyPutOn) {
            play(config.enemySound, config.soundVolume);
        }
    }

    public static void play(AlertSound type, int volume) {
        SoundEvent sound = switch (type) {
            case OFF -> null;
            case ORB -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case PLING -> SoundEvents.NOTE_BLOCK_PLING.value();
            case BELL -> SoundEvents.NOTE_BLOCK_BELL.value();
            case CHIME -> SoundEvents.NOTE_BLOCK_CHIME.value();
            case BASS -> SoundEvents.NOTE_BLOCK_BASS.value();
            case ANVIL -> SoundEvents.ANVIL_LAND;
            case LEVEL_UP -> SoundEvents.PLAYER_LEVELUP;
        };
        if (sound != null && volume > 0) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, volume / 100.0F));
        }
    }
}
