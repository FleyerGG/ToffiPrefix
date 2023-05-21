package ru.fleyer.toffiprefix.chat;

import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.fleyer.toffiprefix.ToffiPrefix;
import ru.fleyer.toffiprefix.utils.ChatUtils;
import ru.fleyer.toffiprefix.utils.ConfigManager;
import ru.fleyer.toffiprefix.utils.CooldownUtils;

import java.util.ArrayList;
import java.util.UnknownFormatConversionException;

/**
 * @author Fleyer
 * <p> ChatManage creation on 25.03.2023 at 21:03
 */
public class ChatManager implements Listener {
    ConfigManager config;

    public ChatManager(ConfigManager manager) {
        config = manager;
    }

    @SneakyThrows
    @EventHandler
    public void sendChat(AsyncPlayerChatEvent event) {
        var format = "";
        var message = event.getMessage();
        val player = event.getPlayer();
        var global = false;
        val global_symbol = config.getString("chat_global_symbol").get();

        if (message.startsWith(global_symbol)) {
            global = true;
            if ((message = message.substring(1)).equalsIgnoreCase("")) {
                global = false;
                message = global_symbol;
            }
        }

        if (global) {
            if (CooldownUtils.hasCooldown(player.getName(), "global")) {
                val sec = CooldownUtils.getCooldown(player.getName(), "global") + 1L;
                player.sendMessage(config.getString("chat_delay_message")
                        .get().replace("%sec%", String.valueOf(sec)));
                event.setCancelled(true);
                return;
            }
            format = config.getString("chat_global_format").get();
        } else {
            if (CooldownUtils.hasCooldown(player.getName(), "local")) {
                var sec = CooldownUtils.getCooldown(player.getName(), "local") + 1L;
                player.sendMessage(config.getString(
                        "chat_delay_message").get().replace("%sec%", String.valueOf(sec)));
                event.setCancelled(true);
                return;
            }

            format = config.getString("chat_local_format").get();
        }
        val spyInfo = ChatUtils.replacePlayerPlaceholders(player, format, true);
        format = ChatUtils.replacePlayerPlaceholders(player, format, false);

        if (!global) {
            event.getRecipients().clear();
            event.getRecipients().addAll(ChatUtils.getPlayersNear(
                    player.getLocation(),
                    (config.getInt("chat_range").get())));
        }

        format = format.replaceAll("  +", " ");
        event.setFormat(format);

        try {
            event.setFormat(format);
        } catch (UnknownFormatConversionException ex) {
            ToffiPrefix.instance.getLogger().warning("Placeholder in format is not allowed!");
            format = format.replaceAll("%\\\\?.*?%", "");
            event.setFormat(format);
        }

        message = event.getPlayer().hasPermission(config.getString("chat_permissions_color").get())
                ? message.replaceAll("&", "§")
                : message.replaceAll("[&§].", "");
        message = message.replaceAll("  +", " ").trim();

        event.setMessage(message);

        if (!player.getName().equals(player.getDisplayName().replaceAll("[&§].", ""))) {
            val admins = new ArrayList<>();
            val finalSpyInfo = spyInfo.replace("%2$s", message);
            val perm = config.getString("chat_permissions_show_realname")
                    .get();

            event.getRecipients().stream().filter(adminPlayer ->
                    adminPlayer.hasPermission(perm)).forEach(adminPlayer -> {
                adminPlayer.sendMessage(finalSpyInfo);
                admins.add(adminPlayer);
            });

            admins.forEach(event.getRecipients()::remove);
        }
        if (global && !player.hasPermission(config.getString("chat_permissions_cooldown_bypass").get())) {
            CooldownUtils.setCooldown(player.getName(),
                    config.getInt("chat_global_delay").get() * 1000L, "global");
        }

        if (!global && !player.hasPermission(config.getString("chat_permissions_cooldown_bypass").get())) {
            CooldownUtils.setCooldown(player.getName(),
                    config.getInt("chat_local_delay").get() * 1000L, "local");
        }
    }

}
