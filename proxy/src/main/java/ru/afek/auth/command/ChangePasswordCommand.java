package ru.afek.auth.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.afek.auth.Auth;
import ru.afek.auth.AuthUser;
import ru.afek.auth.config.SettingsAuth;
import ru.afek.auth.hash.PasswordSecurity;
import ru.afek.bungeecord.commons.MethodCommon;
import ru.afek.bungeecord.commons.StringCommon;

import java.security.NoSuchAlgorithmException;

/**
 * @author Afek
 */

public class ChangePasswordCommand extends Command {

    public ChangePasswordCommand() {
        super("changepassword", null, "cp");
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(this.createMessage("Эта команда только для игроков!"));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(StringCommon.color(SettingsAuth.IMP.CHANGE_PASSWORD_COMMAND.CHANGE_PASSWORD));
            return;
        }

        String oldPassword = args[0];
        String newPassword = args[1];
        if (newPassword.length() < 4 || newPassword.length() > 16) {
            sender.sendMessage(StringCommon.color(SettingsAuth.IMP.CHANGE_PASSWORD_COMMAND.WRONG_LENGHT));
            return;
        }

        if (!MethodCommon.checkMessage(newPassword)) {
            sender.sendMessage(StringCommon.color(SettingsAuth.IMP.CHANGE_PASSWORD_COMMAND.WRONG_CHARS.replace("%valid_chars%", "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")));
            return;
        }

        Auth auth = BungeeCord.getInstance().getAuth();
        AuthUser authUser = auth.getUser(sender.getName());
        boolean hash;
        try {
            hash = PasswordSecurity.comparePasswordWithHash(oldPassword, authUser.getPassword(), sender.getName());
        } catch (Exception ex) {
            return;
        }

        if (!hash) {
            sender.sendMessage(StringCommon.color(SettingsAuth.IMP.CHANGE_PASSWORD_COMMAND.MATCH_ERROR_PWD));
            return;
        }

        String hashPassword;
        try {
            hashPassword = PasswordSecurity.getHash(newPassword, sender.getName());
        } catch (NoSuchAlgorithmException e) {
            return;
        }

        authUser.setPassword(hashPassword);
        auth.saveUser(sender.getName(), authUser);
        sender.sendMessage(StringCommon.color(SettingsAuth.IMP.CHANGE_PASSWORD_COMMAND.CHANGE_PASSWORD_MSG));
    }

    private BaseComponent[] createMessage(final String message) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", SettingsAuth.IMP.PREFIX).replace("%nl%", "\n")));
    }
}
