package ru.fleyer.toffiprefix.commands;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.fleyer.toffiprefix.ToffiPrefix;
import ru.fleyer.toffiprefix.manager.prefixes.PrefixManager;
import ru.fleyer.toffiprefix.utils.ChatUtils;
import ru.fleyer.toffiprefix.utils.ConfigManager;

/**
 * @author Fleyer
 * <p> TestCommand creation on 22.03.2023 at 21:05
 */
public class PrefixesCommand implements CommandExecutor {
    ToffiPrefix toffiPrefix;
    ConfigManager config;

    public PrefixesCommand(ToffiPrefix toffiPrefix, ConfigManager manager) {
        this.toffiPrefix = toffiPrefix;
        config = manager;
    }

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        val player = (Player) sender;
        val prefix = StringUtils.join(args, " ", 1, args.length);

        if (!player.hasPermission(config.getString("command_permission_use").get())) {
            player.sendMessage(ChatUtils.formatterPathColor("command_message_permissions"));
            return false;
        }

        if (args.length < 2) {
            config.getStringList("command_message_help").get()
                    .forEach(s -> {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
                    });
            return false;
        }

        if (!isValidText(prefix) && !player.hasPermission(config.getString("command_permission_bypass").get())) {
            player.sendMessage(ChatUtils.formatterPathColor("command_message_valid_text"));
            return true;
        }
        if (!player.hasPermission(config.getString("command_permission_bypass").get())
                && prefix.length() < config.getInt("command_prefix_min_length").get()) {
            player.sendMessage(ChatUtils.formatterPathColor("command_min_length"));
            return true;
        }
        if (!player.hasPermission(config.getString("command_permission_bypass").get())
                && prefix.length() >= config.getInt("command_prefix_max_length").get()) {

            player.sendMessage(ChatUtils.formatterPathColor("command_max_length"));
            return true;
        }

        switch (args[0]) {
            case "chat": {
                if (hasValidSymbolAfterAmpersand(prefix)) {
                    player.sendMessage(ChatUtils.formatterPathColor("command_chat_valid_symbol_message")
                            .replace("%prefix%", prefix));

                    PrefixManager.setPlayerPrefixes(player,
                            ChatUtils.formatterPathColor("command_chat_valid_symbol_format_prefix")
                                    .replace("%prefix%", prefix), "chat");
                    return true;
                }
                player.sendMessage(ChatUtils.formatterPathColor("command_chat_message")
                        .replace("%prefix%", prefix.replace("&", "§")));
                PrefixManager.setPlayerPrefixes(player,
                        ChatUtils.formatterPathColor("command_chat_format_prefix")
                                .replace("%prefix%", prefix), "chat");
                return true;
            }
            case "tab": {
                if (hasValidSymbolAfterAmpersand(prefix)) {
                    player.sendMessage(ChatUtils.formatterPathColor("command_tab_valid_symbol_message")
                            .replace("%prefix%", prefix));
                    PrefixManager.setPlayerPrefixes(player,
                            ChatUtils.formatterPathColor("command_tab_valid_symbol_format_prefix")
                                    .replace("%prefix%", prefix), "table");
                    return true;
                }
                player.sendMessage(ChatUtils.formatterPathColor("command_tab_message")
                        .replace("%prefix%", prefix.replace("&", "§")));
                PrefixManager.setPlayerPrefixes(player,
                        ChatUtils.formatterPathColor("command_tab_format_prefix")
                                .replace("%prefix%", prefix), "table");

                return true;
            }
            case "all": {
                if (hasValidSymbolAfterAmpersand(prefix)) {
                    player.sendMessage(ChatUtils.formatterPathColor("command_all_valid_symbol_message")
                            .replace("%prefix%", prefix));
                    PrefixManager.setPlayerPrefixes(player,
                            ChatUtils.formatterPathColor("command_all_valid_symbol_format_prefix")
                                    .replace("%prefix%", prefix), "everywhere");

                    return true;
                }
                player.sendMessage(ChatUtils.formatterPathColor("command_all_message")
                        .replace("%prefix%", prefix.replace("&", "§")));
                PrefixManager.setPlayerPrefixes(player,
                        ChatUtils.formatterPathColor("command_all_format_prefix")
                                .replace("%prefix%", prefix), "everywhere");

                return true;
            }
            default: {
                config.getStringList("command_message_help").get()
                        .forEach(s -> {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
                        });
            }
        }
        return false;
    }

    @SneakyThrows
    public boolean isValidText(String text) {
        val regex = config.getString("command_valid_symbols_format").get();
        return text.matches(regex);
    }

    @SneakyThrows
    public boolean hasValidSymbolAfterAmpersand(String text) {
        val regex = config.getString("command_valid_symbols_after_format").get();
        return !text.matches(regex);
    }
}
