package ru.fleyer.toffiprefix.manager.prefixes;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import ru.fleyer.toffiprefix.ToffiPrefix;
import ru.fleyer.toffiprefix.database.DatabaseConstructor;
import ru.fleyer.toffiprefix.utils.ChatUtils;
import ru.fleyer.toffiprefix.utils.ConfigManager;
import ru.fleyer.toffiprefix.utils.LuckPermsUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author Fleyer
 * <p> PrefixManager creation on 22.03.2023 at 18:10
 */
@UtilityClass
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PrefixManager {
    Map<UUID, Prefixes> playerPrefixes = new HashMap<>();
    Map<String, Prefixes> playerInfo = new HashMap<>();

    Scoreboard scoreboard = ToffiPrefix.getInstance().getScoreboard();
    ConfigManager tab = ToffiPrefix.getInstance().getConfigs();

    public void setTabPrefix(Player player, String prefix, int priority, boolean tab, boolean custom) {
        val playerId = player.getUniqueId();
        val currentPrefix = playerPrefixes.get(playerId);
        if (
                currentPrefix == null
                        || !currentPrefix.getPrefix().equals(prefix)
                        || priority != currentPrefix.getPriority()
        ) {

            playerPrefixes.remove(playerId);
            playerPrefixes.put(playerId, new Prefixes(
                    ChatColor.translateAlternateColorCodes('&', prefix),
                    priority)
            );

            updateTabPrefix(player, tab, custom);
        }
    }

    @SneakyThrows
    private void updateTabPrefix(Player player, boolean tabIn, boolean custom) {
        val prefix = playerPrefixes.get(player.getUniqueId());

        if (!custom && prefix != null) {
            var team = scoreboard.getTeam("0" + prefix.getPriority() + ""
                    + LuckPermsUtils.getPlayerGroup(player));
            val playerTeam = scoreboard.getEntryTeam(player.getName());
            if (team == null) {
                team = scoreboard.registerNewTeam("0" + prefix.getPriority() + ""
                        + LuckPermsUtils.getPlayerGroup(player));
            }
            if (playerTeam == null || playerTeam != team) {
                team.addEntry(player.getName());
            }
            player.setPlayerListName(prefix.getPrefix() + player.getDisplayName());
            player.setCustomName(prefix.getPrefix() + player.getDisplayName());
            team.setPrefix(prefix.getPrefix());
            return;
        }
        var team = scoreboard.getTeam("0" + prefix.getPriority() + ""
                + player.getName());
        val playerTeam = scoreboard.getEntryTeam(player.getName());
        if (team == null) {
            team = scoreboard.registerNewTeam("0" + prefix.getPriority() + ""
                    + player.getName());
        }
        if (playerTeam == null || playerTeam != team) {
            team.addEntry(player.getName());
        }

        team.setPrefix(prefix.getPrefix());
        if (tabIn) team.setSuffix(ChatUtils.formatter(player,"tab_suffix_format"));

    }

    @SneakyThrows
    public void setter(Player player) {
        val group = LuckPermsUtils.getPlayerGroup(player);
        val manage = playerInfo.get(group);
        if (DatabaseConstructor.getInstallationPlaceByNickname(player.getName()).get() == null) {
            setTabPrefix(player, manage.getPrefix(), manage.getPriority(), false,false);
            return;
        }

        switch (DatabaseConstructor.getInstallationPlaceByNickname(player.getName()).get()) {
            case "table": {
                setTabPrefix(player, DatabaseConstructor.getPrefixNicknameAndInstallationPlace(
                        player.getName(), "table").get(), manage.getPriority(), true, true);
                return;
            }
            case "chat": {
                setTabPrefix(player, manage.getPrefix(), manage.getPriority(), false, false);
                return;
            }
            case "everywhere": {
                setTabPrefix(
                        player,
                        DatabaseConstructor.getPrefixByNicknameAndPlaceChat(player.getName()).get(),
                        manage.getPriority(),
                        true,true
                );
            }
        }
    }

    @SneakyThrows
    public void load() {
        tab.getConfigurationSection("statuses").get().getKeys(false).forEach(str -> {
            var prefix = "";
            var priority = 0;
            try {
                prefix = tab.getString("statuses." + str + ".prefix").get();
                priority = tab.getInt("statuses." + str + ".priority").get();
                Prefixes prefixes = new Prefixes(
                        prefix, priority);
                playerInfo.put(str, prefixes);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        });
    }

    public void setPlayerPrefixes(Player player, String prefix, String type) {
        switch (type) {
            case "table": {
                DatabaseConstructor.addOrUpdatePlayer(
                        player.getName(),
                        player.getUniqueId().toString(),
                        prefix,
                        "table"
                );
                return;
            }
            case "chat": {
                DatabaseConstructor.addOrUpdatePlayer(
                        player.getName(),
                        player.getUniqueId().toString(),
                        prefix,
                        "chat"
                );
                return;
            }
            case "everywhere": {
                DatabaseConstructor.addOrUpdatePlayer(
                        player.getName(),
                        player.getUniqueId().toString(),
                        prefix,
                        "everywhere"
                );
            }
        }
    }
}
