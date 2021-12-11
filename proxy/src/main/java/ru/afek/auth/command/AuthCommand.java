package ru.afek.auth.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
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
import java.util.Set;

/**
 * @author Afek
 */

public class AuthCommand extends Command {

    public AuthCommand() {
        super("auth", "auth.command.auth", "authorization");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload": {
                if (args.length != 1) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth reload &f- Перезагрузить конфиг"));
                    return;
                }

                BungeeCord bc = BungeeCord.getInstance();
                bc.getAuth().disable();
                bc.setAuth(new Auth(bc.getSqlConnection()));
                break;
            }

            case "stat":
            case "stats": {
                if (args.length != 1) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth stat&f - Показать статистику"));
                    return;
                }

                this.sendStat(sender);
                break;
            }

            case "register":
            case "reg": {
                if (args.length != 3) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth register &f[&6игрок&f] [&6пароль&f] - Зарегистрировать игрока"));
                    return;
                }

                this.tryRegister(sender, args[1], args[2]);
                break;
            }

            case "unregister":
            case "unreg": {
                if (args.length != 2) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth unregister &f[&6игрок&f] - Удалить игрока"));
                    return;
                }

                this.tryUnRegister(sender, args[1]);
                break;
            }

            case "info": {
                if (args.length != 2) {
                    sender.sendMessage(StringCommon.color("%prefix% &6/auth info &f[&6игрок&f] - посмотреть информацию о игроке"));
                    return;
                }

                this.sendPlayerInfo(sender, args[1]);
                break;
            }

            case "changepassword":
            case "cp": {
                if (args.length != 3) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth changepassword &f[&6игрок&f] [&6пароль&f] - Изменить пароль игроку"));
                    return;
                }

                this.changePassword(sender, args[1], args[2]);
                break;
            }

            case "changeemail":
            case "ce": {
                if (args.length != 3) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth changeemail &f[&6игрок&f] [&6пароль&f] - Изменить эл. почту игроку"));
                    return;
                }

                this.changeEmail(sender, args[1], args[2]);
                break;
            }

            case "accountlimit": {
                if (args.length != 3) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth accountlimit &f[&6игрок&f] [&6количество&f] - Изменить количество аккаунтов игроку"));
                    return;
                }

                if (!this.isStringInt(args[2])) {
                    sender.sendMessage(StringCommon.color("%prefix% /auth accountlimit &f[&6игрок&f] [&6количество&f] - Изменить количество аккаунтов игроку"));
                    return;
                }

                this.setAccountIpLimit(sender, args[1], Integer.parseInt(args[2]));
                break;
            }

            default:
                this.sendHelp(sender);
        }
    }

    private void changeEmail(final CommandSender sender, final String name, final String email) {
        Auth auth = BungeeCord.getInstance().getAuth();
        if (!auth.isRegistered(name)) {
            sender.sendMessage(StringCommon.color("&f[&6!&f] &l&fТакой игрок не зарегистрирован!"));
            return;
        }

        AuthUser user = auth.getUser(name);
        user.setEmail(email);
        auth.saveUser(name, user);
        sender.sendMessage(StringCommon.color("&f[&6!&f] &l&fВы успешно сменили почту игроку - &6" + name));
    }

    private void changePassword(final CommandSender sender, final String name, final String pass) {
        Auth auth = BungeeCord.getInstance().getAuth();
        if (!auth.isRegistered(name)) {
            sender.sendMessage(StringCommon.color("&f[&6!&f] &l&fТакой игрок не зарегистрирован!"));
            return;
        }

        String hash;
        try {
            hash = PasswordSecurity.getHash(pass, name);
        } catch (NoSuchAlgorithmException e) {
            return;
        }

        AuthUser user = auth.getUser(name);
        user.setPassword(hash);
        auth.saveUser(name, user);
        sender.sendMessage(StringCommon.color("&f[&6!&f] &l&fВы успешно сменили пароль игроку - &6" + name));
    }

    private void tryUnRegister(CommandSender sender, String name) {
        Auth auth = BungeeCord.getInstance().getAuth();
        if (!auth.isRegistered(name)) {
            sender.sendMessage(StringCommon.color("&f[&6!&f] &l&fТакой игрок не зарегистрирован!"));
            return;
        }

        ProxiedPlayer player = BungeeCord.getInstance().getPlayer(name);
        auth.unRegister(name);
        sender.sendMessage(StringCommon.color("&f[&6!&f] &l&fВы успешно удалили игрока - &6" + name));
        if (player != null)
            player.disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cВаш аккаунт был удален!")));
    }

    private void tryRegister(CommandSender sender, String name, String pass) {
        if (!MethodCommon.checkMessage(pass)) {
            sender.sendMessage(SettingsAuth.IMP.REGISTER.WRONG_CHARS.replace("%valid_chars%", "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"));
            return;
        }

        String hash;
        try {
            hash = PasswordSecurity.getHash(pass, name);
        } catch (NoSuchAlgorithmException e) {
            return;
        }
        final AuthUser user = new AuthUser(name.toLowerCase(), hash, "0.0.0.0", -1L, "null", SettingsAuth.IMP.USER_COUNT);
        BungeeCord.getInstance().getAuth().saveUser(name, user);
        sender.sendMessage(StringCommon.color("&f[&6!&f] &l&fВы успешно зарегистрировали игрока - &6" + name));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(StringCommon.color("&f&m+----&6&m----&f&m----&6&m----&f&m----&6 &f&lAuth by Afek &r&6&m----&f&m----&6&m----&f&m----&6&m----+"));
        sender.sendMessage(StringCommon.color("&6/auth reload&f - Перезагружить конфиг"));
        sender.sendMessage(StringCommon.color("&6/auth stat&f - Статистика"));
        sender.sendMessage(StringCommon.color("&6/auth register &f[&6игрок&f] [&6пароль&f] - Зарегистрировать игрока"));
        sender.sendMessage(StringCommon.color("&6/auth unregister &f[&6игрок&f] - Удалить игрока"));
        sender.sendMessage(StringCommon.color("&6/auth changepassword &f[&6игрок&f] [&6пароль&f] - Изменить пароль игроку"));
        sender.sendMessage(StringCommon.color("&6/auth changeemail &f[&6игрок&f] [&6эл. почта&f] - Изменить почту игроку"));
        sender.sendMessage(StringCommon.color("&6/auth accountlimit &f[&6игрок&f] [&6количество&f] - Изменить количество аккаунтов игроку"));
        sender.sendMessage(StringCommon.color("&6/auth info &f[&6игрок&f] - Посмотреть информацию о игроке"));
    }

    private void sendStat(CommandSender sender) {
        Auth auth = BungeeCord.getInstance().getAuth();
        String prefix = "&f[&6!&f]&l&f ";
        sender.sendMessage(StringCommon.color("&f&m+----&6&m----&f&m----&6&m----&f&m----&6 &f&lAuth by Afek &r&6&m----&f&m----&6&m----&f&m----&6&m----+"));
        sender.sendMessage(StringCommon.color(prefix + "Кол-во игроков в авторизации: &6&l" + auth.getOnlineOnFilter()));
        sender.sendMessage(StringCommon.color(prefix + "Игроки которые прошли авторизацию: &6&l" + auth.getUsersCount()));
        sender.sendMessage(StringCommon.color(prefix + "Игроков зарегистрировано: &6&l" + auth.getRegisteredSize()));
        sender.sendMessage(StringCommon.color(prefix + "Обратится к автору - &6vk.com/afekvova &f| &6afek.ru &f| &6t.me/afekvova"));
    }

    private void sendPlayerInfo(CommandSender sender, String name) {
        final Auth auth = BungeeCord.getInstance().getAuth();
        if (!auth.isRegistered(name.toLowerCase())) {
            sender.sendMessage(StringCommon.color("&f[&6!&f]&l&f Такого игрока нету в базе данных!"));
            return;
        }

        final String ip = auth.getUser(name).getIp();
        sender.sendMessage(StringCommon.color("&f&m+----&6&m----&f&m----&6&m----&f&m----&6 &f&lИнформация о игроке - &6" + name + " &r&6&m----&f&m----&6&m----&f&m----&6&m----+"));
        sender.sendMessage(StringCommon.color(" (1) Айпи: &6" + ip));
        final Set<String> similarPlayers = auth.getSql().getEqualIp(name, ip);
        String message = similarPlayers.isEmpty() ? "Игрок зарегистрировал только 1 аккаунт!" : String.join(", ", similarPlayers);
        sender.sendMessage(" (2) Игроки с идентичным адресом: " + message);
    }

    private void setAccountIpLimit(CommandSender sender, String name, int limit) {
        Auth auth = BungeeCord.getInstance().getAuth();
        sender.sendMessage(StringCommon.color("&f[&6!&f]&l&f Вы успешно изменили количество аккаунтов игроку - &6" + name));
        auth.getSql().saveUserIpLimit(name, limit);
    }

    public boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
