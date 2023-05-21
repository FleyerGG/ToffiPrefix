package ru.fleyer.toffiprefix.manager.prefixes;

import lombok.SneakyThrows;
import lombok.val;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.fleyer.toffiprefix.database.DatabaseConstructor;
import ru.fleyer.toffiprefix.utils.LuckPermsUtils;

/**
 * @author Fleyer
 * <p> PrefixPlaceholderManager creation on 22.03.2023 at 21:12
 */
public class PrefixPlaceholderManager extends PlaceholderExpansion {

    @SneakyThrows
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        if (p == null) {
            return "";
        }
        if (ind.contains("prefix")) {
            val group = LuckPermsUtils.getPlayerGroup(p);
            val manage = PrefixManager.playerInfo.get(group);

            if (DatabaseConstructor.getInstallationPlaceByNickname(p.getName()).get() == null){
                return manage.getPrefix();
            }

            switch (DatabaseConstructor.getInstallationPlaceByNickname(p.getName()).get()) {
                case "table": {
                    return PrefixManager.playerInfo.get(LuckPermsUtils.getPlayerGroup(p)).getPrefix();
                }
                case "chat": {
                    return DatabaseConstructor.getPrefixFromNicknameTable(p, "chat");
                }
                case "everywhere": {
                    return DatabaseConstructor.getPrefixByNicknameAndPlaceChat(p.getName()).get();
                }
            }
        }
        return ind;
    }

    public @NotNull String getVersion() {
        return "1.0";
    }

    public String getPlugin() {
        return "ToffiPrefix";
    }

    public @NotNull String getIdentifier() {
        return "toffiprefix";
    }

    public @NotNull String getAuthor() {
        return "Fleyer001";
    }
}
