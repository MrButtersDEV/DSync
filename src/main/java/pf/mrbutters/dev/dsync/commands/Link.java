package pf.mrbutters.dev.dsync.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
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

            // Tell player their link code or if they are linked
            if (!DSync.isLinked(player.getUniqueId())) {
                TextComponent message = new TextComponent( ChatColor.RED + "Please send " + ChatColor.DARK_RED + "\"!link {code}\"".replace("{code}", code) + ChatColor.RED + " to our discord bot!");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.COPY_TO_CLIPBOARD, "!link {code}".replace("{code}", code)) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( ChatColor.GOLD + "Click 2 Copy" )));
                player.sendMessage(message);
                // Set player's link code
                DSync.linkCode.put(player, code);
            } else {
                TextComponent message = new TextComponent(ChatColor.RED + "You are already verified. Type " + ChatColor.DARK_RED + "/unlink" + ChatColor.RED + " to unlink your account.");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.SUGGEST_COMMAND, "/unlink") );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( ChatColor.GOLD + "/unlink" )));
                player.sendMessage(message);
            }

        } else {
            System.out.println("Only players can link their MC account with Discord!");
        }
    }

}
