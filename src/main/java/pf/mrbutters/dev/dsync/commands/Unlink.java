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

import java.util.UUID;

public class Unlink extends Command{

    public Unlink() {
        super("unlink");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (DSync.isLinked(player.getUniqueId())) {
                DSync.unlinkViaUUID(player.getUniqueId());
                TextComponent message = new TextComponent(ChatColor.AQUA + "You have successfully unlinked your MC account with Discord!");
                player.sendMessage(message);
            } else {
                TextComponent message = new TextComponent(ChatColor.RED + "You are are not verified. Type " + ChatColor.DARK_RED + "/link" + ChatColor.RED + " if you wish to link your accounts.");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.SUGGEST_COMMAND, "/link") );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( ChatColor.GOLD + "/link" )));
                player.sendMessage(message);
            }
        } else {
            System.out.println("Only players can link their MC account with Discord!");
        }
    }

}
