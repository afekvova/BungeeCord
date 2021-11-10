package ru.afek.auth.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.afek.auth.Auth;
import ru.afek.auth.AuthUser;
import ru.afek.auth.config.SettingsAuth;
import ru.afek.auth.hash.RandomString;
import ru.afek.bungeecord.commons.StringCommon;

/**
 * @author Afek
 */

public class EmailCommand extends Command {

    public EmailCommand() {
        super("email", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            SettingsAuth.IMP.EMAIL_COMMAND.EMAIL_HELP.forEach(msg -> sender.sendMessage(StringCommon.color(msg)));
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("&cYou aren't player!");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "verify": {
                if (args.length != 2) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_VERIFY_COMMAND.EMAIL_VERIFY));
                    return;
                }

                Auth auth = BungeeCord.getInstance().getAuth();
                if (!auth.getVerifyCode().containsKey(sender.getName())) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_VERIFY_COMMAND.NO_REQUEST));
                    return;
                }

                if (!auth.getVerifyCode().get(sender.getName()).getCode().equals(args[1])) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_VERIFY_COMMAND.INVALID_CODE));
                    return;
                }

                AuthUser authUser = auth.getUser(sender.getName());
                authUser.setEmail(auth.getVerifyCode().get(sender.getName()).getEmail());
                auth.saveUser(sender.getName(), authUser);
                sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_VERIFY_COMMAND.EMAIL_VERIFY_MSG));
                break;
            }

            case "change": {
                if (args.length != 3) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_CHANGE_COMMAND.EMAIL_CHANGE));
                    return;
                }

                AuthUser authUser = BungeeCord.getInstance().getAuth().getUser(sender.getName());
                if (authUser.getEmail().equalsIgnoreCase("null")) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_CHANGE_COMMAND.NO_EMAIL));
                    return;
                }

                if (!authUser.getEmail().equalsIgnoreCase(args[1])) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_CHANGE_COMMAND.INVALID_OLD_EMAIL));
                    return;
                }

                if (!args[2].contains("@")) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_CHANGE_COMMAND.INVALID_NEW_EMAIL));
                    return;
                }

                Auth auth = BungeeCord.getInstance().getAuth();
                RandomString rand = new RandomString(8);
                String code = rand.nextString();
                auth.sendVerifyCode(sender.getName(), code, args[2]);
                sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_CHANGE_COMMAND.EMAIL_CHANGE_MSG));
                break;
            }

            case "add": {
                if (args.length != 3) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_ADD_COMMAND.EMAIL_ADD));
                    return;
                }

                AuthUser authUser = BungeeCord.getInstance().getAuth().getUser(sender.getName());
                if (!authUser.getEmail().equalsIgnoreCase("null")) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_ADD_COMMAND.HAVE_EMAIL));
                    return;
                }

                if (!args[1].equalsIgnoreCase(args[2])) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_ADD_COMMAND.MATCH_ERROR_EMAIL));
                    return;
                }

                if (!args[1].contains("@")) {
                    sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_ADD_COMMAND.INVALID_EMAIL));
                    return;
                }

                Auth auth = BungeeCord.getInstance().getAuth();
                RandomString rand = new RandomString(8);
                String code = rand.nextString();
                auth.sendVerifyCode(sender.getName(), code, args[1]);
                sender.sendMessage(StringCommon.color(SettingsAuth.IMP.EMAIL_ADD_COMMAND.EMAIL_ADD_MSG));
                break;
            }
            default:
                SettingsAuth.IMP.EMAIL_COMMAND.EMAIL_HELP.forEach(msg -> sender.sendMessage(StringCommon.color(msg)));
        }
    }
}
