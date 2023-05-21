package ru.fleyer.toffiprefix.manager.board;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.fleyer.toffiprefix.utils.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Fleyer
 * <p> BoardManger creation on 30.03.2023 at 10:49
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BoardManager {
    WorldGuardPlugin wg;
    Map<UUID, Board> board;
    ConfigManager file;
    Map<String, BoardsInfo> info;

    @SneakyThrows
    public void updater(Player player) {
        RegionManager rgm = wg.getRegionManager(player.getWorld());
        ApplicableRegionSet ars = rgm.getApplicableRegions(player.getLocation());
        val boards = board.get(player.getUniqueId());

        if (ars.getRegions().size() == 0) {
            boards.updateTitle(formatterString(player, "base_title"));
            boards.updateLines(formatter(player, "base_lines"));
            return;
        }
        for (val protectedRegion : ars.getRegions()) {
            for (val values : info.values()) {
                if (values.getRgName().equals(protectedRegion.getId())
                        && protectedRegion.getType() == RegionType.CUBOID) {
                    boards.updateTitle(
                            ChatColor.translateAlternateColorCodes('&',
                                    replace(player, values.getTitle())));
                    boards.updateLines(formatterList(player, values.getLines()));
                    return;
                }
            }
        }
    }

    @SneakyThrows
    public List<String> formatter(Player player, String path) {
        val list = new ArrayList<String>();
        file.getStringList(path).get().forEach(s -> list.add(ChatColor.translateAlternateColorCodes('&',
                replace(player, s))));
        return list;

    }

    @SneakyThrows
    public List<String> formatterList(Player player, List<String> lines) {
        val list = new ArrayList<String>();
        lines.forEach(s -> list.add(ChatColor.translateAlternateColorCodes('&',
                replace(player, s))));
        return list;
    }


    @SneakyThrows
    public String formatterString(Player player, String path) {
        return ChatColor.translateAlternateColorCodes('&', replace(player,
                file.getString(path).get()));

    }

    public String replace(Player player, String text) {
        var holder = text;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            System.out.println("Плагин PlaceholderAPI не найден, пожалуйста установите, для нормальной работы.");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
                && PlaceholderAPI.containsPlaceholders(holder)
        ) {
            holder = PlaceholderAPI.setPlaceholders(player, holder);
        }
        return holder;
    }
}
