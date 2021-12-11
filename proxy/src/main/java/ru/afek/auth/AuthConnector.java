package ru.afek.auth;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.*;
import ru.afek.auth.config.SettingsAuth;
import ru.afek.auth.hash.PasswordSecurity;
import ru.afek.auth.utils.BlackListIp;
import ru.afek.bungeecord.commons.MethodCommon;
import ru.afek.bungeecord.commons.StringCommon;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.caching.PacketsPosition;
import ru.leymooo.botfilter.utils.FailedUtils;
import ru.leymooo.botfilter.utils.IPUtils;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthConnector extends MoveHandlerAuth {

    private static final Logger LOGGER = BungeeCord.getInstance().getLogger();
    private final Auth auth;
    private UserConnection userConnection;
    private final String name, ip;
    private Auth.CheckStateAuth state;
    private int version, errors;
    private Channel channel;
    private boolean markDisconnected;
    private long lastSend, totalping, joinTime;

    public AuthConnector(UserConnection userConnection, Auth auth) {
        this.lastSend = 0L;
        this.totalping = 9999L;
        this.state = Auth.CheckStateAuth.REGISTER;
        this.errors = 5;
        this.joinTime = System.currentTimeMillis();
        this.markDisconnected = false;
        Preconditions.checkNotNull(auth, "Auth instance is null");
        this.auth = auth;
        this.name = userConnection.getName();
        this.channel = userConnection.getCh().getHandle();
        this.userConnection = userConnection;
        this.version = userConnection.getPendingConnection().getVersion();
        this.userConnection.setClientEntityId(PacketUtils.CLIENTID);
        this.userConnection.setDimension(0);
        this.ip = IPUtils.getAddress(this.userConnection).getHostAddress();
        if (!auth.isRegistered(this.name)) {
            this.state = Auth.CheckStateAuth.REGISTER;
        } else {
            this.state = Auth.CheckStateAuth.LOGIN;
        }
    }

    public void spawn(boolean captcha) {
        if (!captcha)
            PacketUtils.spawnPlayer(this.channel, userConnection.getPendingConnection().getVersion(), true, false);
        sendPing();
        this.channel.writeAndFlush(PacketUtils.getCachedPacket(PacketsPosition.SETSLOT_RESET).get(this.version));
        LOGGER.log(Level.INFO, toString() + " has connected");
    }

    public long getJoinTime() {
        return this.joinTime;
    }

    @Override
    public void exception(final Throwable t) {
        this.markDisconnected = true;
        this.userConnection.disconnect(Util.exception(t));
        this.disconnected();
    }

    public UserConnection getUserConnection() {
        return this.userConnection;
    }

    @Override
    public void disconnected(final ChannelWrapper channel) {
        this.auth.removeConnection(null, this);
        this.disconnected();
    }

    @Override
    public void handlerChanged() {
        this.disconnected();
    }

    private void disconnected() {
        this.channel = null;
        this.userConnection = null;
    }

    public void completeCheck(boolean login) {
        this.channel.flush();
        this.state = Auth.CheckStateAuth.SUCCESSFULLY;
        this.auth.removeConnection(null, this);
        if (login) {
            this.sendMessage(SettingsAuth.IMP.LOGIN.LOGIN_SUCCESS);
        } else {
            this.sendMessage(SettingsAuth.IMP.REGISTER.REG_SUCCESS);
        }
        AuthUser user = this.auth.getUser(this.name);
        user.setSession(System.currentTimeMillis());
        user.setIp(this.ip);
        this.auth.saveUser(this.getName(), user);
        this.userConnection.setNeedLogin(false);
        this.userConnection.getPendingConnection().finishLogin(this.userConnection, true);
        this.markDisconnected = true;
        AuthConnector.LOGGER.log(Level.INFO, "[Auth] Игрок (" + this.name + "|" + this.ip + ") успешно прошёл проверку");
    }

    @Override
    public void onMove() {
        if (this.lastY == -1.0 || this.onGround) {
            return;
        }
        this.resetPosition(true);
    }

    private void resetPosition(final boolean disableFall) {
        if (disableFall)
            this.channel.write(PacketUtils.getCachedPacket(PacketsPosition.PLAYERABILITIES).get(this.version), this.channel.voidPromise());
        this.waitingTeleportId = 9876;
        this.channel.writeAndFlush(PacketUtils.getCachedPacket(PacketsPosition.PLAYERPOSANDLOOK).get(this.version), this.channel.voidPromise());
    }

    @Override
    public void handle(TabCompleteRequest response) {
        response.setAssumeCommand(false);
    }

    @Override
    public void handle(TabCompleteResponse response) {
        response.getSuggestions().getList().add(new Suggestion(new StringRange(1, 1), "login"));
    }

    @Override
    public void handle(final Chat chat) throws Exception {
        final String chatmessage = chat.getMessage();
        if (chatmessage.length() > 256) {
            this.failed(PacketUtils.KickType.FAILED_CAPTCHA, "Too long message");
            return;
        }

        if (!chatmessage.startsWith("/")) {
            this.sendMessage(SettingsAuth.IMP.ERROR.DENIED_CHAT);
            return;
        }

        final String[] message = chatmessage.split(" ");
        final String command = message[0].replaceFirst("/", "");
        final String[] args = new String[message.length - 1];
        System.arraycopy(message, 1, args, 0, message.length - 1);
        final String lowerCase = command.toLowerCase();
        switch (lowerCase) {
            case "recovery": {
                if (this.state == Auth.CheckStateAuth.SUCCESSFULLY) return;

                if (this.state == Auth.CheckStateAuth.REGISTER && !this.auth.isRegistered(this.name)) {
                    this.sendMessage(SettingsAuth.IMP.LOGIN.NOT_REGISTERED);
                    return;
                }

                if (args.length != 1) {
                    this.sendMessage(SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.EMAIL_RECOVERY);
                    return;
                }

                AuthUser authUser = this.auth.getUser(this.name);
                if (authUser.getEmail().equalsIgnoreCase("null")) {
                    this.sendMessage(SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.NO_EMAIL);
                    return;
                }

                if (this.auth.sendEmailRecovery(this.name))
                    this.sendMessage(SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.NEW_EMAIL_MSG);
                break;
            }

            case "l":
            case "login": {
                if (this.state == Auth.CheckStateAuth.SUCCESSFULLY) {
                    this.sendMessage(SettingsAuth.IMP.LOGIN.ALREADY_LOG);
                    return;
                }

                if (this.state == Auth.CheckStateAuth.REGISTER && !this.auth.isRegistered(this.name)) {
                    this.sendMessage(SettingsAuth.IMP.LOGIN.NOT_REGISTERED);
                    return;
                }

                if (args.length != 1) {
                    this.sendMessage(SettingsAuth.IMP.LOGIN.USAGE_LOG);
                    return;
                }

                this.tryAuth(args[0]);
                break;
            }
            case "reg":
            case "register": {
                if (this.state == Auth.CheckStateAuth.SUCCESSFULLY) {
                    this.sendMessage(SettingsAuth.IMP.REGISTER.ALREADY_REG);
                    return;
                }

                if (this.state == Auth.CheckStateAuth.LOGIN) {
                    this.sendMessage(SettingsAuth.IMP.REGISTER.ALREADY_REG_NOT);
                    return;
                }

                if (args.length < 2) {
                    this.sendMessage(SettingsAuth.IMP.REGISTER.USAGE_REG);
                    return;
                }

                if (!args[0].equals(args[1])) {
                    this.sendMessage(SettingsAuth.IMP.REGISTER.MATCH_ERROR_PWD);
                    return;
                }

                if (args[0].length() < 4 || args[0].length() > 16) {
                    this.sendMessage(SettingsAuth.IMP.REGISTER.WRONG_LENGHT);
                    return;
                }

                this.tryRegister(args[0]);
                break;
            }
            default: {
                this.sendMessage(SettingsAuth.IMP.ERROR.DENIED_CMD);
                break;
            }
        }
    }

    public Auth.CheckStateAuth getState() {
        return this.state;
    }

    private void tryRegister(final String pass) {
        if (!MethodCommon.checkMessage(pass)) {
            this.sendMessage(SettingsAuth.IMP.REGISTER.WRONG_CHARS.replace("%valid_chars%", "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"));
            return;
        }

        String hash;
        try {
            hash = PasswordSecurity.getHash(pass, this.name);
        } catch (NoSuchAlgorithmException e) {
            return;
        }

        final AuthUser user = new AuthUser(this.name.toLowerCase(), hash, this.ip, System.currentTimeMillis(), "null", SettingsAuth.IMP.USER_COUNT);
        this.auth.saveUser(this.name, user);
        this.state = Auth.CheckStateAuth.SUCCESSFULLY;
        this.completeCheck(false);
    }

    private void tryAuth(final String pass) {
        boolean hash;
        try {
            hash = PasswordSecurity.comparePasswordWithHash(pass, this.auth.getUser(this.name).getPassword(), this.name);
        } catch (Exception ex) {
            return;
        }
        if (hash) {
            this.state = Auth.CheckStateAuth.SUCCESSFULLY;
            this.completeCheck(true);
        } else {
            --this.errors;
            this.sendMessage(SettingsAuth.IMP.LOGIN.WRONG_PWD);
            if (this.errors == 3) {
                this.replaceTry(this.errors);
            }

            if (this.errors == 2) this.addError();
            if (this.errors == 1) this.addError();

            if (this.errors == 0) {
                this.state = Auth.CheckStateAuth.FAILED;
                addError();
                this.userConnection.getCh().close(PacketUtils.createKickPacket("&cВы забанены на 10 минут."));
            }
        }
    }

    private void addError() {
        BlackListIp.IncreaseOrAdd(IPUtils.getAddress(this.ip));
        AuthUser authUser = this.auth.getUser(this.name);
        if (!authUser.getEmail().equalsIgnoreCase("null")) {
            PacketUtils.actionBar.writeActionBar(channel, version);
            this.sendMessage(SettingsAuth.IMP.LOGIN.EMAIL_RECOVERY);
        }
        this.replaceTry(this.errors);
    }

    public void replaceTry(final int trying) {
        this.sendMessage(SettingsAuth.IMP.LOGIN.TRYING.replace("%try%", trying + "").replace("%padezh%", StringCommon.padezh("\u043f\u043e\u043f\u044b\u0442", "\u043a\u0430", "\u043a\u0438", "\u043e\u043a", trying)));
    }

    @Override
    public void handle(final ClientSettings settings) throws Exception {
        this.userConnection.setSettings(settings);
        this.userConnection.setCallSettingsEvent(true);
    }

    @Override
    public void handle(final KeepAlive keepAlive) throws Exception {
        if (keepAlive.getRandomId() == PacketUtils.KEEPALIVE_ID) {
            if (this.lastSend == 0L) {
                this.failed(PacketUtils.KickType.PING, "Tried send fake ping");
                return;
            }

            final long ping = System.currentTimeMillis() - this.lastSend;
            this.totalping = ((this.totalping == 9999L) ? ping : (this.totalping + ping));
            this.lastSend = 0L;
        }
    }

    @Override
    public void handle(final PluginMessage pluginMessage) throws Exception {
        if (!userConnection.getPendingConnection().relayMessage0(pluginMessage)) {
            userConnection.addDelayedPluginMessage(pluginMessage);
        }
    }

    public String getName() {
        return this.name.toLowerCase();
    }

    public boolean isConnected() {
        return this.userConnection != null && this.channel != null && !this.markDisconnected && this.userConnection.isConnected();
    }

    public void sendPing() {
        if (this.lastSend == 0L && this.state != Auth.CheckStateAuth.FAILED && this.state != Auth.CheckStateAuth.SUCCESSFULLY) {
            this.lastSend = System.currentTimeMillis();
            this.channel.writeAndFlush(PacketUtils.getCachedPacket(PacketsPosition.KEEPALIVE).get(this.version));
        }
    }

    public void failed(final PacketUtils.KickType type, final String kickMessage) {
        PacketUtils.kickPlayer(type, Protocol.GAME, this.userConnection.getCh(), this.version);
        this.markDisconnected = true;
        AuthConnector.LOGGER.log(Level.INFO, "[" + this.name + "|" + this.ip + "] check failed: " + kickMessage);
        FailedUtils.addIpToQueue(this.ip, type);
    }

    public void sendMessage(final String message) {
        this.channel.write(PacketUtils.createMessagePacketAuth(message), this.channel.voidPromise());
    }

    public void sendMessage(final int index) {
        final ByteBuf buf = PacketUtils.getCachedPacket(index).get(this.getVersion());
        if (buf != null) this.getChannel().write(buf, this.getChannel().voidPromise());
    }

    @Override
    public String toString() {
        return "[" + this.name + "|" + this.ip + "] <-> Auth";
    }

    public int getVersion() {
        return this.version;
    }

    public Channel getChannel() {
        return this.channel;
    }
}
