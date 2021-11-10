package ru.afek.auth.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.checkerframework.checker.units.qual.A;
import ru.afek.auth.Auth;
import ru.afek.auth.AuthUser;
import ru.afek.auth.config.SettingsAuth;
import ru.afek.bungeecord.commons.StringCommon;
import ru.leymooo.botfilter.config.Settings;

/**
 * @author Afek
 */

public class RecoveryCommand extends Command {

    public RecoveryCommand() {
        super("recovery", null);
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(this.createMessage("Эта команда только для игроков!"));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.EMAIL_RECOVERY));
            return;
        }

        Auth auth = BungeeCord.getInstance().getAuth();
        AuthUser authUser = auth.getUser(sender.getName());
        if (authUser.getEmail().equalsIgnoreCase("null")) {
            sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.NO_EMAIL));
            return;
        }

        if (auth.sendEmailRecovery(sender.getName()))
            sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.NEW_EMAIL_MSG));
    }

    private BaseComponent[] createMessage(final String message) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", SettingsAuth.IMP.PREFIX).replace("%nl%", "\n")));
    }
}
