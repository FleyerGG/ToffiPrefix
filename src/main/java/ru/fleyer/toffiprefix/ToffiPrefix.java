package ru.fleyer.toffiprefix;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import ru.fleyer.toffiprefix.chat.ChatManager;
import ru.fleyer.toffiprefix.chat.CommandBlockerManager;
import ru.fleyer.toffiprefix.commands.PrefixesCommand;
import ru.fleyer.toffiprefix.database.DatabaseConstructor;
import ru.fleyer.toffiprefix.events.BaseListener;
import ru.fleyer.toffiprefix.manager.board.Board;
import ru.fleyer.toffiprefix.manager.board.BoardManager;
import ru.fleyer.toffiprefix.manager.board.BoardsInfo;
import ru.fleyer.toffiprefix.manager.prefixes.PrefixManager;
import ru.fleyer.toffiprefix.manager.prefixes.PrefixPlaceholderManager;
import ru.fleyer.toffiprefix.utils.ConfigManager;
import ru.fleyer.toffiprefix.utils.LuckPermsUtils;
import ru.fleyer.toffiprefix.utils.TablistPacketsUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ToffiPrefix extends JavaPlugin {
    @Getter
    public static ToffiPrefix instance;
    @Getter
    final Map<UUID, Board> boards = new HashMap<>();
    @Getter
    final Map<String, BoardsInfo> info = new HashMap<>();
    @Getter
    BukkitTask scoreboardTask;
    @Getter
    Scoreboard scoreboard;
    @Getter
    ConfigManager configs;
    @Getter
    ConfigManager replacements;
    @Getter
    ConfigManager blockerCommand;
    @Getter
    ConfigManager board;

    @SneakyThrows
    @Override
    public void onEnable() {
        instance = this;
        createBaseComponents();

        LuckPermsUtils.registerLuckPerms();
        PrefixManager.load();
        load();
        starterBoard();
        new PrefixPlaceholderManager().register();

        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this, new TablistPacketsUtils(), 0, 20);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
                Bukkit.getOnlinePlayers().forEach(PrefixManager::setter),
                0, configs.getInt("tab_refresh_period").get());

        DatabaseConstructor.setConnection(
                configs.getString("maridb_user").get(),
                configs.getString("maridb_password").get(),
                configs.getInt("maridb_port").get(),
                configs.getString("maridb_dbname").get(),
                configs.getString("maridb_host").get()
        );
        DatabaseConstructor.createTableIfNotExists();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (scoreboardTask != null) {
            scoreboardTask.cancel();
        }
    }

    void createBaseComponents() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        configs = new ConfigManager(this, "config.yml", false);
        replacements = new ConfigManager(this, "replacements.yml", false);
        blockerCommand = new ConfigManager(this, "commandblocker.yml", false);
        board = new ConfigManager(this, "board.yml", false);

        getCommand("toffiprefix").setExecutor(new PrefixesCommand(this, configs));

        Bukkit.getPluginManager().registerEvents(new BaseListener(boards), this);
        Bukkit.getPluginManager().registerEvents(new ChatManager(configs), this);
        Bukkit.getPluginManager().registerEvents(new CommandBlockerManager(blockerCommand), this);
    }

    @SneakyThrows
    void starterBoard() {
        scoreboardTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Board boardResult : this.boards.values()) {
                new BoardManager(
                        getWG(),
                        boards,
                        board,
                        info
                ).updater(boardResult.getPlayer());
            }
        }, 0L, (long) configs.getInt("boards_refresh").get());
    }

    @SneakyThrows
    void load() {
        for (val s : board.getConfigurationSection("boards").get().getKeys(false)) {
            val rgName = board.getString("boards." + s + ".region").get();
            val title = board.getString("boards." + s + ".title").get();
            val lines = board.getStringList("boards." + s + ".lines").get();
            val boardsInfo = new BoardsInfo(rgName, title, lines);
            info.put(s, boardsInfo);
        }
    }

    WorldGuardPlugin getWG() {
        val plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (plugin == null) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
}
