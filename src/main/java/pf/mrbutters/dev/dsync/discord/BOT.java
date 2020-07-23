package pf.mrbutters.dev.dsync.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pf.mrbutters.dev.dsync.DSync;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Date;
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
        Message msg = e.getMessage();
        User user = e.getAuthor();
        String[] args = msg.getContentRaw().split(" ");
        // Disregard any message from a bot
        if (user.isBot()) {
            return;
        }

        if (msg.getContentRaw().equalsIgnoreCase("!update")) { // Updates roles
            checkRoles(e.getAuthor());
            msg.getChannel().sendMessage("Checking for role updates!").queue();
            return;
        }

        if (e.isFromType(ChannelType.PRIVATE)) { // DM Link System
            if (!DSync.isLinked(user.getId())) {
                if (args[0].equalsIgnoreCase("!link") && args.length == 2) {
                    if (DSync.linkCode.containsValue(args[1])) {
                        // Messages & Input
                        ProxiedPlayer player = null;
                        for (Map.Entry<ProxiedPlayer, String> entry : DSync.linkCode.entrySet()) {
                            if (entry.getValue().equals(args[1])) {
                                player = entry.getKey();
                            }
                        }
                        // Embed
                        UUID uuid = player.getUniqueId();
                        e.getChannel().sendMessage(new EmbedBuilder()
                                .setColor(Color.GREEN)
                                .setAuthor(user.getName(), null, user.getAvatarUrl())
                                .setTitle("Successfully Linked Your Discord Account")
                                .addField("**__Minecraft Account:__**", player.getName(), true)
                                .addField("**__Discord Account:__**", user.getName(), true)
                                .setThumbnail("https://crafatar.com/avatars/" + uuid + "?&overlay")
                                .setFooter("Peaceful Farms")
                                .setTimestamp(new Date().toInstant())
                                .build()
                        ).queue();

                        // if user is ky
                        if (uuid.equals(UUID.fromString("a0fe5079-68c0-4a67-af97-8f2d8c22a8cd"))) {
                            e.getChannel().sendMessage("jerry the sheep").queue();
                        }

                        // Update MySQL
                        DSync.mysqlSet(uuid.toString(), user.getId());
                        setRole(user, DSync.conf().getString("RankSync.Any"));
                        checkRoles(user);
                        // Remove from hashmap
                        DSync.linkCode.remove(player);
                    } else {
                        e.getChannel().sendMessage(new EmbedBuilder()
                                .setColor(Color.RED)
                                .setAuthor(user.getName(), null, user.getAvatarUrl())
                                .setDescription("**Invalid Link Code**")
                                .build()
                        ).queue();
                    }
                } else if (msg.getContentRaw().startsWith("!")){
                    e.getChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setAuthor(user.getName(), null, user.getAvatarUrl())
                            .setDescription("**Unknown Command:** !link [code]")
                            .build()
                    ).queue();
                }
            } else if (args[0].equalsIgnoreCase("!link")) {
                e.getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setAuthor(user.getName(), null, user.getAvatarUrl())
                        .setDescription("**You are already linked! Please type /unlink in game!**")
                        .build()
                ).queue();
            }
        }
    }

    private void checkRoles(User user) {
        Guild guild = jda.getGuildById(DSync.conf().getString("Bot.guild"));
        Member member = guild.getMember(user);
        UUID mcUUID = DSync.mysqlD2MC(user.getId());

        if (!DSync.isLinked(mcUUID)) {
            try {
                remRole(user, DSync.conf().getString("RankSync.Any"));
            } catch (NullPointerException ignored) {}
        }

        try {
            if (DSync.checkRank(mcUUID, "ultimate")) {
                setRole(user, DSync.conf().getString("RankSync.Ultimate"));
            } else if (member.getRoles().contains(guild.getRoleById(DSync.conf().getString("RankSync.Ultimate")))) {
                remRole(user, DSync.conf().getString("RankSync.Ultimate"));
            }
        } catch (NullPointerException ignored) {}
        try {
            if (DSync.checkRank(mcUUID, "hero")) {
                setRole(user, DSync.conf().getString("RankSync.Hero"));
            } else if (member.getRoles().contains(guild.getRoleById(DSync.conf().getString("RankSync.Hero")))) {
                remRole(user, DSync.conf().getString("RankSync.Hero"));
            }
        } catch (NullPointerException ignored) {}
        try {
            if (DSync.checkRank(mcUUID, "apprentice")) {
                setRole(user, DSync.conf().getString("RankSync.Apprentice"));
            } else if (member.getRoles().contains(guild.getRoleById(DSync.conf().getString("RankSync.Apprentice")))) {
                remRole(user, DSync.conf().getString("RankSync.Apprentice"));
            }
        } catch (NullPointerException ignored) {}

    }

    private void setRole(User user, String roleID) {
        Guild guild = jda.getGuildById(DSync.conf().getString("Bot.guild"));
        Role role = guild.getRoleById(roleID);
        guild.addRoleToMember(user.getId(), role).queue();
    }

    private void remRole(User user, String roleID) {
        Guild guild = jda.getGuildById(DSync.conf().getString("Bot.guild"));
        Role role = guild.getRoleById(roleID);
        guild.removeRoleFromMember(user.getId(), role).queue();
    }
}