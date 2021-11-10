package ru.afek.auth;

import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

import net.md_5.bungee.BungeeCord;
import ru.afek.auth.config.SettingsAuth;
import ru.leymooo.botfilter.caching.PacketUtils;

public class AuthThread {

    private static Thread thread;
    private static final HashSet<String> TO_REMOVE_SET = new HashSet<>();
    private static BungeeCord bungee = BungeeCord.getInstance();

    public static void start() {
        (thread = new Thread(() -> {
            while (sleep(2000L)) {
                try {
                    long currTime = System.currentTimeMillis();
                    for (Map.Entry<String, AuthConnector> entryset : bungee.getAuth().getConnectedUsersSet().entrySet()) {
                        AuthConnector connector = entryset.getValue();
                        if (!connector.isConnected()) {
                            TO_REMOVE_SET.add(entryset.getKey());
                            continue;
                        }

                        Auth.CheckStateAuth state = connector.getState();
                        switch (state) {
                            case SUCCESSFULLY:
                            case FAILED:
                                TO_REMOVE_SET.add(entryset.getKey());
                                continue;
                            default:
                                if ((currTime - connector.getJoinTime()) >= SettingsAuth.IMP.TIME_OUT) {
                                    connector.getUserConnection().getCh().close(PacketUtils.createKickPacket(SettingsAuth.IMP.LOGIN.TIMEOUT));
                                    TO_REMOVE_SET.add(entryset.getKey());
                                    continue;
                                } else if (state == Auth.CheckStateAuth.REGISTER) {
                                    connector.sendMessage(SettingsAuth.IMP.REGISTER.USAGE_REG);
                                } else if (state == Auth.CheckStateAuth.LOGIN) {
                                    connector.sendMessage(SettingsAuth.IMP.LOGIN.USAGE_LOG);
                                }

                                connector.sendPing();
                        }
                    }
                } catch (Exception e) {
                    bungee.getLogger().log(Level.WARNING, "[Auth] Ошибка в потоке: ", e);
                } finally {
                    if (!TO_REMOVE_SET.isEmpty()) {
                        for (String remove : TO_REMOVE_SET)
                            bungee.getAuth().removeConnection(remove, null);
                        TO_REMOVE_SET.clear();
                    }
                }
            }
        }, "Auth thread")).start();
    }

    public static void stop() {
        if (thread != null) thread.interrupt();
    }

    private static boolean sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            return false;
        }
        return true;
    }
}
