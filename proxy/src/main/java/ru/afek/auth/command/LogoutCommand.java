package ru.afek.auth.command;

import net.md_5.bungee.api.chat.BaseComponent;
import ru.afek.auth.AuthUser;
import ru.afek.auth.Auth;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import ru.afek.auth.config.SettingsAuth;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Afek
 */

public class LogoutCommand extends Command {

    public LogoutCommand() {
        super("logout", null, "exit");
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(this.createMessage("Эта команда только для игроков!"));
            return;
        }

        if (args.length == 0) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            Auth auth = BungeeCord.getInstance().getAuth();
            AuthUser user = auth.getUser(player.getName());
            user.logout();
            auth.saveUser(player.getName(), user);
            player.disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', SettingsAuth.IMP.LOGOUT.LOG_SUCCESS)));
        } else {
            sender.sendMessage(this.createMessage(SettingsAuth.IMP.LOGOUT.USAGE_LOG));
        }
    }

    private BaseComponent[] createMessage(final String message) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", SettingsAuth.IMP.PREFIX).replace("%nl%", "\n")));
    }
}
