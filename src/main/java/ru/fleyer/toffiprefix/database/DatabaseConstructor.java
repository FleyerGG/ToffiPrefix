package ru.fleyer.toffiprefix.database;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Fleyer
 * <p> DatabaseConstructor creation on 22.03.2023 at 21:13
 */
@UtilityClass
public class DatabaseConstructor {
    Connection connection;

    @SneakyThrows
    public void setConnection(String username, String password, int port, String dbname, String host) {
        val bukkit = Bukkit.getServer();
        Class.forName("org.mariadb.jdbc.Driver");

        connection = DriverManager.getConnection(
                "jdbc:mariadb://" + host + ":" + port + "/" + dbname,
                username,
                password);
        bukkit.getLogger().info("Database connection established.");
    }


    public void createTableIfNotExists() {
        val sql = "CREATE TABLE IF NOT EXISTS nickname_table ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "nickname VARCHAR(50) NOT NULL,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "prefix VARCHAR(100) NOT NULL,"
                + "installation_place ENUM('chat', 'table', 'everywhere') NOT NULL"
                + ");";

        CompletableFuture.runAsync(() -> {
            try (val stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
                Bukkit.getServer().getLogger().info(ChatColor.RED + "Юхуу я создал таблицу");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String getPrefixFromNicknameTable(Player player, String installationPlace) {
        val sql = "SELECT prefix FROM nickname_table WHERE uuid = ? AND installation_place = ?";

        val future = CompletableFuture.supplyAsync(() -> {
            try (val stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, installationPlace);
                return stmt.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });

        try {
            val rs = future.get();
            if (rs != null && rs.next()) {
                return rs.getString("prefix");
            }
        } catch (InterruptedException | ExecutionException | SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public CompletableFuture<String> getPrefixNicknameAndInstallationPlace(String nickname, String installationPlace) {
        return CompletableFuture.supplyAsync(() -> {
            try (val statement = connection.prepareStatement(
                    "SELECT prefix FROM nickname_table WHERE nickname = ? AND installation_place = ?")) {
                statement.setString(1, nickname);
                statement.setString(2, installationPlace);

                val resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("prefix");
                } else {
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<String> getPrefixByNicknameAndPlaceChat(String nickname) {
        val future = new CompletableFuture<String>();
        val sql = "SELECT prefix FROM nickname_table WHERE nickname = ?" +
                " AND installation_place IN ('chat', 'everywhere')";
        try (val stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nickname);
            val rs = stmt.executeQuery();
            if (rs.next()) {
                val prefix = rs.getString("prefix");
                future.complete(prefix);
            } else {
                future.completeExceptionally(new RuntimeException("No matching records found."));
            }
        } catch (SQLException e) {
            future.completeExceptionally(e);
        }
        return future;
    }


    public void addOrUpdatePlayer(
            String nickname,
            String uuid,
            String prefix,
            String installationPlace
    ) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // Check if the player already exists in the table
                val selectStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM nickname_table WHERE nickname = ?");
                selectStatement.setString(1, nickname);
                val resultSet = selectStatement.executeQuery();

                int count = 0;
                if (resultSet.next()) {
                    count = resultSet.getInt("count");
                }

                PreparedStatement statement;
                if (count > 0) {
                    // If the player already exists, update their installation_place and prefix
                    statement = connection.prepareStatement(
                            "UPDATE nickname_table SET installation_place = ?, prefix = ? WHERE nickname = ?");
                    statement.setString(1, installationPlace);
                    statement.setString(2, prefix);
                    statement.setString(3, nickname);
                } else {
                    // If the player doesn't exist, insert a new row
                    statement = connection.prepareStatement(
                            "INSERT INTO nickname_table (nickname, uuid, prefix, installation_place)" +
                                    " VALUES (?, ?, ?, ?)");
                    statement.setString(1, nickname);
                    statement.setString(2, uuid);
                    statement.setString(3, prefix);
                    statement.setString(4, installationPlace);
                }

                int rowsAffected = statement.executeUpdate();

                return rowsAffected > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public CompletableFuture<String> getInstallationPlaceByNickname(String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                val statement = connection.prepareStatement(
                        "SELECT installation_place FROM nickname_table WHERE nickname = ?");
                statement.setString(1, nickname);

                val resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("installation_place");
                } else {
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
