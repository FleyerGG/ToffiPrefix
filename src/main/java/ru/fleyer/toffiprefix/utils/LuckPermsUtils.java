package ru.fleyer.toffiprefix.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Fleyer
 * <p> LuckPermsUtils creation on 22.03.2023 at 18:18
 */
@UtilityClass
public class LuckPermsUtils {
    public LuckPerms api;

    public void registerLuckPerms() {
        val provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();

        }
    }

    public String getPlayerGroup(Player player) {
        val userManager = api.getUserManager().getUser(player.getUniqueId());

        if (userManager != null) {
            return userManager.getNodes().stream()
                    .filter(NodeType.INHERITANCE::matches)
                    .map(NodeType.INHERITANCE::cast)
                    .map(InheritanceNode::getGroupName)
                    .findFirst()
                    .orElse("default");
        } else {
            return "default";
        }
    }
}
