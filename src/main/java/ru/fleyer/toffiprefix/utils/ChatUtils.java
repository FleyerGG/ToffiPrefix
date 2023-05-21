package ru.fleyer.toffiprefix.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.fleyer.toffiprefix.ToffiPrefix;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fleyer
 * <p> ChatUtils creation on 25.03.2023 at 21:05
 */
@UtilityClass
public class ChatUtils {
    ToffiPrefix toffiPrefix = ToffiPrefix.getInstance();
    ConfigManager replacements = toffiPrefix.getReplacements();

    public List<Player> getPlayersNear(Location playerLocation, int range) {
        val players = new ArrayList<Player>();
        val squaredDistance = Math.pow(range, 2.0);
        for (val player : playerLocation.getWorld().getPlayers()) {
            if (range > 0 && playerLocation.distanceSquared(player.getLocation()) > squaredDistance) continue;
            players.add(player);
        }
        return players;
    }

    public boolean checkPlaceholderAPI() {
        val plugin = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        return plugin != null && plugin.isEnabled();
    }

    @SneakyThrows
    public String replacePlaceholder(Player player, String placeholder) {
        placeholder = PlaceholderAPI.setPlaceholders(player, placeholder);
        for (val key : replacements.getAllKeys().get()) {
            placeholder = placeholder.replace(replacements
                    .getString(key + ".original").get(), replacements.getString(key + ".text").get());
        }
        return placeholder;
    }

    @SneakyThrows
    public String replacePlayerPlaceholders(Player player, String format, boolean b) {
        if (player == null) {
            return format;
        }

        var result = format;
        val config = toffiPrefix.getConfigs();
        var prefix = config.getString("chat_placeholder_prefix").get();
        var suffix = config.getString("chat_placeholder_suffix").get();

        if (checkPlaceholderAPI()) {
            result = PlaceholderAPI.setPlaceholders(player, result);
            suffix = replacePlaceholder(player, suffix);
            prefix = replacePlaceholder(player, prefix);
        }

        result = b
                ? result.replace(
                "{player}", player.getDisplayName()
                        + " (" + player.getName() + ")")
                : result.replace("{player}", player.getDisplayName());

        result = result.replace("{message}", "%2$s");
        result = result.replace("{prefix}", prefix);
        result = result.replace("{suffix}", suffix);

        return result;
    }

    @SneakyThrows
    public String formatter(Player player, String string) {
        return ChatColor.translateAlternateColorCodes('&',
                PlaceholderAPI.setPlaceholders(player, toffiPrefix.getConfigs().getString(string).get())
        );
    }

    @SneakyThrows
    public String formatterPathColor(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                toffiPrefix.getConfigs().getString(path).get()
        );
    }

}
