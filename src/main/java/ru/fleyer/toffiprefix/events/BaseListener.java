package ru.fleyer.toffiprefix.events;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.fleyer.toffiprefix.manager.board.Board;
import ru.fleyer.toffiprefix.manager.prefixes.PrefixManager;

import java.util.Map;
import java.util.UUID;

/**
 * @author Fleyer
 * <p> Events creation on 22.03.2023 at 18:14
 */
public class BaseListener implements Listener {
    Map<UUID, Board> boardMap;

    public BaseListener(Map<UUID, Board> boardMap) {
        this.boardMap = boardMap;
    }

    @SneakyThrows
    @EventHandler
    public void join(PlayerJoinEvent event) {
        val player = event.getPlayer();
        val board = new Board(player);

        board.updateTitle("Загрузка...");

        boardMap.put(player.getUniqueId(), board);

        PrefixManager.setter(player);
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        val player = e.getPlayer();

        val board = boardMap.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
}
