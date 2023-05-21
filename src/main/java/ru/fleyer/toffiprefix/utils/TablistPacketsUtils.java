package ru.fleyer.toffiprefix.utils;

import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;

/**
 * @author Fleyer
 * <p> TablisPacketsUtils creation on 21.03.2023 at 20:47
 */
public class TablistPacketsUtils implements Runnable {
    PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

    @SneakyThrows
    @Override
    public void run() {
        try {
            val a = packet.getClass().getDeclaredField("a");
            a.setAccessible(true);
            val b = packet.getClass().getDeclaredField("b");
            b.setAccessible(true);

            final ChatComponentText[] footer = {new ChatComponentText("")};
            final ChatComponentText[] header = {new ChatComponentText("")};
            Bukkit.getOnlinePlayers().forEach(s -> {
                header[0] = new ChatComponentText(ChatUtils.formatter(s, "tab_header"));
                footer[0] = new ChatComponentText(ChatUtils.formatter(s, "tab_footer"));
            });

            a.set(packet, header[0]);
            b.set(packet, footer[0]);

            if (Bukkit.getOnlinePlayers().size() == 0) return;
            Bukkit.getOnlinePlayers().forEach(player ->
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
