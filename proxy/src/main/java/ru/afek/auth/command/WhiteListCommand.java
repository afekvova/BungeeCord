package ru.afek.auth.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.afek.auth.utils.WhiteList;
import ru.afek.bungeecord.commons.StringCommon;

import java.util.*;

/**
 * @author Afek
 */

public class WhiteListCommand extends Command {

    public WhiteListCommand() {
        super("whitelist", "auth.command.whitelist", "wl", "whitelistauth");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(StringCommon.color("&6/whitelist [list/add/remove] &f- Работа с вайтлистом"));
            return;
        }

        WhiteList whiteList = BungeeCord.getInstance().getAuth().getWhiteList();

        switch (args[0].toLowerCase()) {
            case "add": {
                if (args.length != 2) {
                    sender.sendMessage(StringCommon.color("&6/whitelist add [ник] &f- Добавить игрока в вайтлист"));
                    return;
                }

                whiteList.addPlayer(args[1]);
                sender.sendMessage(StringCommon.color("&f[&6i&f] Вы успешно добавили игрока " + args[1]));
                break;
            }

            case "remove": {
                if (args.length != 2) {
                    sender.sendMessage(StringCommon.color("&6/whitelist remove [ник] &f- Удалить игрока в вайтлист"));
                    return;
                }

                String nick = args[1];
                if (!whiteList.isPlayer(nick)) {
                    sender.sendMessage(StringCommon.color("&f[&6i&f] Игрока нету в листе"));
                    return;
                }

                whiteList.removePlayer(nick);
                sender.sendMessage(StringCommon.color("&f[&6i&f] Вы успешно удалили игрока " + nick));
                break;
            }

            case "list": {
                if (whiteList.getPlayers().isEmpty()) {
                    sender.sendMessage(StringCommon.color("&cЛист пуст!"));
                    return;
                }

                if (args.length == 1) {
                    this.sendPlayerList(sender, 1, whiteList.getPlayers());
                } else {
                    if (!isInteger(args[1])) {
                        sender.sendMessage(StringCommon.color("&6/whitelist list [номер страницы] &f- Список игроков в вайтлисте"));
                        return;
                    }

                    this.sendPlayerList(sender, Integer.parseInt(args[1]), whiteList.getPlayers());
                }
            }

            default:
                sender.sendMessage(StringCommon.color("&6/whitelist [list/add/remove] &f- Работа с вайтлистом"));
        }
    }

    private void sendPlayerList(CommandSender player, int page, Set<String> players) {
        List<List<String>> list = getPages(players, 5);
        int pages = list.size();
        int numPages = (int) Math.ceil(players.size() / 5);

        if (page < 0) {
            player.sendMessage("Минимальное количество страниц: 1");
            return;
        }

        if (page > pages) {
            player.sendMessage("Максимальное количество страниц: " + numPages);
            return;
        }

        List<String> pageList = list.get(page - 1);
        int i = (page - 1) * 5;
        for (String name : pageList) {
            player.sendMessage(i + ". " + name);
            i++;
        }
    }

    private <T> List<List<T>> getPages(Collection<T> c, Integer pageSize) {
        if (c == null)
            return Collections.emptyList();
        List<T> list = new ArrayList<>(c);
        if (pageSize == null || pageSize <= 0 || pageSize > list.size())
            pageSize = list.size();
        int numPages = (int) Math.ceil((double) list.size() / (double) pageSize);
        List<List<T>> pages = new ArrayList<List<T>>(numPages);
        for (int pageNum = 0; pageNum < numPages; )
            pages.add(list.subList(pageNum * pageSize, Math.min(++pageNum * pageSize, list.size())));
        return pages;
    }

    private boolean isInteger(String s) {
        Scanner sc = new Scanner(s.trim());
        if (!sc.hasNextInt(10)) return false;
        sc.nextInt(10);
        return !sc.hasNext();
    }
}
