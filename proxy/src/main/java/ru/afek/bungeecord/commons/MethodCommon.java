package ru.afek.bungeecord.commons;

import io.netty.channel.Channel;
import net.md_5.bungee.UserConnection;
import ru.leymooo.botfilter.caching.PacketUtils;

import java.util.Random;

/**
 * @author Afek
 */

public class MethodCommon {

    public static boolean checkMessage(String message) {
        for (int i = 0; i < message.length(); ++i) {
            if (!"abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".contains(String.valueOf(message.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static void sendMessage(final String message, UserConnection userConnection) {
        Channel channel = userConnection.getCh().getHandle();
        channel.write(PacketUtils.createMessagePacketAuth(message), channel.voidPromise());
    }
}
