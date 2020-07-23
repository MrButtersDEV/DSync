package pf.mrbutters.dev.dsync;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.YamlConfiguration;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import pf.mrbutters.dev.dsync.commands.Link;
import pf.mrbutters.dev.dsync.commands.Unlink;
import pf.mrbutters.dev.dsync.discord.BOT;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

public final class DSync extends Plugin {

    private BOT bot = null;
    private static HikariDataSource hikari;
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
        hikari.addDataSourceProperty("serverName", conf.get("MySQL.address"));
        hikari.addDataSourceProperty("port", conf.get("MySQL.port"));
        hikari.addDataSourceProperty("databaseName", conf.get("MySQL.database"));
        hikari.addDataSourceProperty("user", conf.get("MySQL.username"));
        hikari.addDataSourceProperty("password", conf.get("MySQL.password"));

        createTable();

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

    }

    public HikariDataSource getHikari(){
        return hikari;
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
        } catch (SQLException e) {
            System.out.println("[DSync] Failed to create database tablet!");
            e.printStackTrace();
        }
    }

    public static void mysqlSet(String uuid, String discordid){
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO discord_link(id, PlayerUUID, DiscordID) VALUES(DEFAULT, '"+uuid+"','"+discordid+"')");
        } catch (SQLException e) {
            System.out.println("[DSync] Failed to link user!");
            e.printStackTrace();
        }
    }

    public static UUID mysqlD2MC(String discordID) {
        System.out.println("DiscordID: " + discordID);
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

    public static Configuration conf() {
        return conf;
    }
}
