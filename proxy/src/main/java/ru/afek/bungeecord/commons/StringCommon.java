package ru.afek.bungeecord.commons;

import net.md_5.bungee.api.ChatColor;
import ru.afek.auth.config.SettingsAuth;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Afek
 */

public class StringCommon {

    /**
     * by Bars Склоняем слова правильно
     *
     * @param ed неизменяемая часть слова, которую нужно просклонять
     * @param a  окончание для слова, в случае если число оканчивается на 1
     * @param b  окончание для слова, в случае если число оканчивается на 2, 3
     *           или 4
     * @param c  окончание для слова, в случае если число оканчивается на 0,
     *           5...9 и 11...19
     * @param n  число, по которому идёт склонение
     * @return правильно просклонённое слово по числу
     */
    public static String padezh(String ed, String a, String b, String c,
                                int n) {
        if (n < 0)
            n = -n;
        int last = n % 100;
        if (last > 10 && last < 21)
            return ed + c;
        last = n % 10;
        if (last == 0 || last > 4)
            return ed + c;
        if (last == 1)
            return ed + a;
        if (last < 5)
            return ed + b;
        return ed + c;
    }

    public static String color(String message) {
        if (message == null) return "";
        if (message.isEmpty()) return "";
        message = message.replace("%prefix%", SettingsAuth.IMP.PREFIX);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
