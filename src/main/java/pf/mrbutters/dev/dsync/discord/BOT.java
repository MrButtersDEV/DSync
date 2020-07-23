package pf.mrbutters.dev.dsync.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import pf.mrbutters.dev.dsync.DSync;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class BOT extends ListenerAdapter{

    private String token;
    private JDA jda;

    public BOT(String token) {
        this.token = token;
    }

    public void start(){
        try {
            this.jda = JDABuilder.createDefault(token).build();
            jda.addEventListener(this);
            System.out.println("[DSync] Starting DSync bot...");
        } catch (LoginException err) {
            System.out.println("[DSync] Failed to start discord bot....");
        }
    }

    public void stop(){
        this.jda.shutdown();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.isFromType(ChannelType.PRIVATE)) {
            Message msg = e.getMessage();
            User user = e.getAuthor();

            // Disregard any message from a bot
            if (user.isBot()) {
                return;
            }

            String[] args = msg.getContentRaw().split(" ");

            if (args[0].equalsIgnoreCase("!link") && args.length==2) {
                if (DSync.linkCode.containsValue(args[1])) {

                    // Messages & Input
                    ProxiedPlayer player = null;
                    for (Map.Entry<ProxiedPlayer, String> entry : DSync.linkCode.entrySet()) {
                        if (entry.getValue().equals(args[1])) {
                            player = entry.getKey();
                        }
                    }

                    UUID uuid = player.getUniqueId();
                    e.getChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setAuthor(user.getName(), null, user.getAvatarUrl())
                            .setDescription("Discord Successfully Linked!")
                            .setImage("https://crafatar.com/avatars/"+uuid+"?&overlay")
                            .build()
                    ).queue();

                    // Update MySQL
                    DSync.mysqlSet(uuid.toString(), user.getId());
                    setRole(user, DSync.conf().getString("RankSync.Any"));
                }
            } else {
                e.getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setAuthor(user.getName(), null, user.getAvatarUrl())
                        .setDescription("**Unknown Command:** !verify [code]")
                        .build()
                ).queue();
                checkDonorRoles(user);
            }
        }
    }

    private void checkDonorRoles(User user) {
        //SELECT id, PlayerUUID, DiscordID FROM discord_link WHERE PlayerUUID = '414b8002-6099-4003-a913-8eda65e2b9ac' AND DiscordID = '129618266703265793';
        // -> SELECT PlayerUUID FROM discord_link WHERE DiscordID = '129618266703265793';
        UUID mcUUID = DSync.mysqlD2MC(user.getId());
        System.out.println("MC UUID: " + mcUUID.toString());
    }

    private void setRole(User user, String roleID) {
        Guild guild = jda.getGuildById("422473748130562059");
        Role role = guild.getRoleById(roleID);
        guild.addRoleToMember(user.getId(), role).queue();
    }
}