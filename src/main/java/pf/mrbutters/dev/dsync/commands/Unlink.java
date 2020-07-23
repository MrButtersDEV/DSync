package pf.mrbutters.dev.dsync.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Unlink extends Command{

    public Unlink() {
        super("unlink");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            player.sendMessage(new ComponentBuilder("Username: " + player.getUUID()).color(ChatColor.DARK_AQUA).create());

        } else {
            System.out.println("Only players can link their MC account with Discord!");
        }
    }

}
