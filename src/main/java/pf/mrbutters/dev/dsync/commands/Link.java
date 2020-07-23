package pf.mrbutters.dev.dsync.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import pf.mrbutters.dev.dsync.DSync;
import pf.mrbutters.dev.dsync.utils.LinkCode;

public class Link extends Command{

    public Link() {
        super("link");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            String code = LinkCode.get();

            // Tell player their link code
            player.sendMessage(new ComponentBuilder(ChatColor.RED+"Please send " + ChatColor.DARK_RED + "\"!link {code}\"".replace("{code}", code) + ChatColor.RED + " to our discord bot!").create());

            // Set player's link code
            DSync.linkCode.put(player, code);

        } else {
            System.out.println("Only players can link their MC account with Discord!");
        }
    }

}
