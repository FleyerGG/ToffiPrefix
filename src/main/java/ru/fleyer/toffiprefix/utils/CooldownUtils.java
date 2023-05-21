package ru.fleyer.toffiprefix.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;

/**
 * @author Fleyer
 * <p> CooldownUtils creation on 25.03.2023 at 21:04
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CooldownUtils {
    static HashMap<String, CooldownUtils> cooldowns = new HashMap<>();
    String player;
    long cooldown;
    String key;

    public static void setCooldown(String player, long cooldown, String title) {
        cooldowns.put(player + title, new CooldownUtils(player,
                System.currentTimeMillis() + cooldown,
                title));
    }

    public static boolean hasCooldown(String player, String title) {
        return cooldowns.get(player + title) != null
                && (cooldowns.get(player + title)).getCooldown() > System.currentTimeMillis();
    }

    public static long getCooldown(String player, String title) {
        return ((cooldowns.get(player + title)).getCooldown() - System.currentTimeMillis()) / 1000L;
    }
}
