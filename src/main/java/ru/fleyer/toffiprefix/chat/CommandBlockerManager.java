package ru.fleyer.toffiprefix.chat;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.fleyer.toffiprefix.utils.ConfigManager;

/**
 * @author Fleyer
 * <p> CommandBlockerManager creation on 27.03.2023 at 11:19
 */
public class CommandBlockerManager implements Listener {
    ConfigManager file;

    public CommandBlockerManager(ConfigManager manager) {
        file = manager;
    }

    @SneakyThrows
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        val player = event.getPlayer();
        val command = event.getMessage().split(" ")[0]; // Получаем название команды
        val msg = event.getMessage();

        for (val strings : file.getConfigurationSection("commands").get().getKeys(false)) {
            for (val cmd : file.getStringList("commands." + strings + ".commands").get()) {
                val modifyCommand = cmd.replace("*", "");
                if (
                        msg.equalsIgnoreCase(modifyCommand) &&
                                !hasPermission(player, strings) ||
                                command.equalsIgnoreCase(modifyCommand)
                                        && !hasPermission(player, strings)
                ) {
                    if (cmd.endsWith("*") && modifyCommand.contains(command)) {
                        // Блокируем выполнение команды
                        event.setCancelled(true);
                        // Отправляем игроку сообщение о блокировке команды
                        event.getPlayer().sendMessage(message(strings));
                        return;
                    }

                    if (modifyCommand.contains(msg)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(message(strings));
                        return;
                    }
                    return;
                }
            }
        }
    }

    @SneakyThrows
    public Boolean hasPermission(Player player, String strings) {
        return player.hasPermission(file.getString("commands." + strings + ".permission").get());
    }

    @SneakyThrows
    public String message(String strings) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                file.getString("commands." + strings + ".message").get());
    }
}
