package pf.mrbutters.dev.dsync;

import com.zaxxer.hikari.HikariDataSource;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.YamlConfiguration;
import pf.mrbutters.dev.dsync.commands.Link;
import pf.mrbutters.dev.dsync.commands.Unlink;
import pf.mrbutters.dev.dsync.discord.BOT;

import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

public final class DSync extends Plugin {

    private static BOT bot = null;
    private static HikariDataSource hikari;
    private static HikariDataSource hikari_luckperms;
    private static Configuration conf;
    public static HashMap<ProxiedPlayer, String> linkCode = new HashMap<>();

    @Override
    public void onEnable() {

        // Config
        registerConfig();
        File config = new File(getDataFolder(), "config.yml");
        conf = null;
        try {
            conf = YamlConfiguration.getProvider(YamlConfiguration.class).load(config);
        } catch (IOException err) {
            System.out.println("[DSync] Unable to load config!");
        }

        // Discord BOT
        bot = new BOT(conf.getString("Bot.token"));
        bot.start();

        // MySQL
        hikari = new HikariDataSource();
        hikari.setJdbcUrl("jdbc:mysql://" + conf.get("MySQL.address") + ":" + conf.get("MySQL.port") + "/" + conf.get("MySQL.database") + "?autoReconnect=true&useSSL=false");
        hikari.addDataSourceProperty("user", conf.get("MySQL.username"));
        hikari.addDataSourceProperty("password", conf.get("MySQL.password"));

        // MySQL - LuckPerms
        hikari_luckperms = new HikariDataSource();
        hikari_luckperms.setJdbcUrl("jdbc:mysql://" + conf.get("GroupsMySQL.address") + ":" + conf.get("GroupsMySQL.port") + "/" + conf.get("GroupsMySQL.database") + "?autoReconnect=true&useSSL=false");
        hikari_luckperms.addDataSourceProperty("user", conf.get("GroupsMySQL.username"));
        hikari_luckperms.addDataSourceProperty("password", conf.get("GroupsMySQL.password"));

        createTable(); // Creates the discord_link table if one does not exist

        // Commands
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Link());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Unlink());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        bot.stop();

        if (hikari!=null) {
            hikari.close();
        }

        if (hikari_luckperms!=null) {
            hikari_luckperms.close();
        }

    }

    public HikariDataSource getHikari(){
        return hikari;
    }

    public HikariDataSource getHikariLuckperms() {
        return hikari_luckperms;
    }

    private void registerConfig(){
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createTable(){
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){  // varchar(36)
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS discord_link(id int AUTO_INCREMENT PRIMARY KEY, PlayerUUID text, DiscordID text)");
            System.out.println("[DSync] Creating a database table if one does not exist!");
            statement.close();
        } catch (SQLException e) {
            System.out.println("[DSync] Failed to create database tablet!");
            e.printStackTrace();
        }
    }

    public static void mysqlSet(String uuid, String discordid){
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO discord_link(id, PlayerUUID, DiscordID) VALUES(DEFAULT, '"+uuid+"','"+discordid+"')");
            statement.close();
        } catch (SQLException e) {
            System.out.println("[DSync] Failed to link user!");
            e.printStackTrace();
        }
    }

    public static UUID mysqlD2MC(String discordID) {
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){
            ResultSet rs;
            rs = statement.executeQuery("SELECT PlayerUUID FROM discord_link WHERE DiscordID = '"+discordID+"';");
            if(rs.next()){
                return UUID.fromString(rs.getString(1));
            } else {return null;}
        } catch (SQLException e) {
            System.out.println("[DSync] Failed to convert DiscordID to MC-UUID");
            e.printStackTrace();
            return null;
        }
    }

    public static String mysqlMC2D(UUID PlayerUUID) {
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery("SELECT DiscordID FROM discord_link WHERE PlayerUUID = '"+PlayerUUID+"';");
            if(rs.next()){
                return (rs.getString(1));
            } else {return null;}
        } catch (SQLException e) {
            System.out.println("[DSync] Failed to convert MC-UUID to DiscordID");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkRank(UUID mcUUID, String rank){
        try(Connection connection = hikari_luckperms.getConnection();
            Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery("SELECT * FROM `luckperms_user_permissions` WHERE uuid = '" + mcUUID+ "' AND permission = 'group." + rank +"';");
            return rs.next();
        } catch (SQLException e) {
            System.out.println("[DSync] Failed to check LuckPerms rank! [UUID: " + mcUUID + " Rank: " + rank + "]");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isLinked(UUID mcUUID) {
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery("SELECT DiscordID FROM discord_link WHERE PlayerUUID = '"+mcUUID+"';");
            return rs.next();
        } catch (SQLException e) {
            System.out.println("[DSync-UUID] Failed to check if user is linked!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isLinked(String discordID) {
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery("SELECT PlayerUUID FROM discord_link WHERE DiscordID = '"+discordID+"';");
            return rs.next();
        } catch (SQLException e) {
            System.out.println("[DSync-DiscordID] Failed to check if user is linked!");
            e.printStackTrace();
            return false;
        }
    }

    public static void unlinkViaUUID(UUID mcUUID) {
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){
            statement.executeUpdate("DELETE FROM discord_link WHERE PlayerUUID = '"+mcUUID+"';");
            statement.close();
        } catch (SQLException e) {
            System.out.println("[DSync-DiscordID] Failed to unlink account! [UUID: "+mcUUID+"]");
            e.printStackTrace();
        }
    }

    public static Configuration conf() {
        return conf;
    }
}
