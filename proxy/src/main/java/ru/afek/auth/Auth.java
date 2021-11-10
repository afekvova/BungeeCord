package ru.afek.auth;

import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.netty.HandlerBoss;
import ru.afek.auth.config.SettingsAuth;
import ru.afek.auth.hash.PasswordSecurity;
import ru.afek.auth.hash.RandomString;
import ru.afek.auth.utils.*;
import ru.afek.bungeecord.SQLConnection;
import ru.afek.bungeecord.commons.StringCommon;

import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Afek
 */

public class Auth {

    @Getter
    private final Map<String, AuthConnector> connectedUsersSet;
    @Getter
    private final Map<String, AuthUser> userCache;
    @Getter
    private final Map<String, VerifyCode> verifyCode;
    @Getter
    private final SQLConnectionAuth sql;
    @Getter
    private final EmailSystem emailSystem;
    @Getter
    private final IpListCheck ipListCheck;
    @Getter
    private final WhiteList whiteList;

    public Auth(SQLConnection sql) {
        this.connectedUsersSet = new ConcurrentHashMap<>();
        this.userCache = new ConcurrentHashMap<>();
        this.verifyCode = new ConcurrentHashMap<>();
        this.sql = new SQLConnectionAuth(this, sql);
        this.ipListCheck = new IpListCheck(sql);
        this.emailSystem = new EmailSystem();
        this.whiteList = new WhiteList();
        AuthThread.start();
    }

    public void disable() {
        AuthThread.stop();
        BlackListIp.cleanUP();
        for (final AuthConnector connector : this.connectedUsersSet.values()) {
            if (connector.getUserConnection() != null)
                connector.getUserConnection().disconnect(StringCommon.color("&f[&6Auth&f] Перезагрузка авторизации"));
        }
        this.whiteList.saveUsers();
        this.verifyCode.clear();
        this.connectedUsersSet.clear();
    }

    public void connectToAuth(UserConnection userConnection, boolean captcha) {
        userConnection.setSession(false);
        AuthConnector authConnector = new AuthConnector(userConnection, this);

        if (!addConnection(authConnector)) {
            userConnection.disconnect(BungeeCord.getInstance().getTranslation("already_connected_proxy")); // TODO: Cache this disconnect packet
        } else {
            userConnection.getCh().getHandle().pipeline().get(HandlerBoss.class).setHandler(authConnector);
            authConnector.spawn(captcha);
        }
    }

    public void sendEmailMsg(UserConnection userConnection) {
        AuthUser authUser = this.getUser(userConnection.getName());
        if (authUser == null) return;
        if (!authUser.getEmail().equalsIgnoreCase("null")) return;
        SettingsAuth.IMP.LOGIN.EMAIL_ADD_MSG.forEach(msg -> userConnection.sendMessage(StringCommon.color(msg)));
    }

    public boolean sendEmailRecovery(String name) {
        AuthUser authUser = this.getUser(name);
        if (authUser.getEmail().equalsIgnoreCase("null")) return false;
        RandomString rand = new RandomString(SettingsAuth.IMP.EMAIL.PASSWORD_LENGHT);
        String password = rand.nextString();
        String hash;
        try {
            hash = PasswordSecurity.getHash(password, name);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }

        authUser.setPassword(hash);
        this.saveUser(name, authUser);
        this.getEmailSystem().sendNewPasswordEmailMessage(authUser.getEmail(), authUser.getEmail(), password);
        return true;
    }

    /**
     * Отправить игроку код подтверждения на почту
     *
     * @param playerName Имя игрока
     * @param code       Код подтверждения
     * @param email      Новая эл. почта
     */
    public void sendVerifyCode(String playerName, String code, String email) {
        this.emailSystem.sendVerifyCodeEmailMessage(playerName, email, code);
        this.verifyCode.put(playerName, new VerifyCode(code, email));
    }

    /**
     * Количество пользователей, которые зарегистрировались
     *
     * @return количество пользователей, которые зарегистрировались
     */
    public int getRegisteredSize() {
        return this.userCache.size();
    }

    /**
     * Сохраняет игрока в памяти и в датебазе
     *
     * @param userName Имя игрока
     * @param user     Игрок
     */
    public void saveUser(String userName, AuthUser user) {
        userName = userName.toLowerCase();
        if (this.userCache.containsKey(userName))
            this.userCache.remove(userName);

        this.userCache.put(userName, user);
        if (this.sql != null)
            this.sql.saveUserAuth(user);
    }

    /**
     * Удаляет игрока с памяти и с датебазе
     *
     * @param name Имя игрока
     */
    public void unRegister(String name) {
        if (!this.isRegistered(name)) return;
        this.removeUser(name);
        this.sql.removeUserAuth(name);
    }

    /**
     * Проверить игрока на регистрацию
     *
     * @return Проверить игрока на регистрацию
     */
    public boolean isRegistered(String userName) {
        return this.userCache.containsKey(userName.toLowerCase());
    }

    /**
     * Получить пользователя
     *
     * @return получить пользователя
     */
    public AuthUser getUser(String name) {
        return this.userCache.getOrDefault(name.toLowerCase(), null);
    }

    public void addUserToCache(AuthUser botFilterUser) {
        this.userCache.put(botFilterUser.getName(), botFilterUser);
    }

    /**
     * Удаляет игрока из памяти
     *
     * @param userName Имя игрока, которого следует удалить из памяти
     */
    public void removeUser(String userName) {
        userName = userName.toLowerCase();
        this.userCache.remove(userName);
    }

    /**
     * Добавляет игрока в мапу
     *
     * @param connector connector
     * @return если игрок был добавлен в мапу
     */
    public boolean addConnection(AuthConnector connector) {
        return connectedUsersSet.putIfAbsent(connector.getName().toLowerCase(), connector) == null;
    }

    /**
     * Убирает игрока из мапы.
     *
     * @param name      Имя игрока (lowercased)
     * @param connector Объект коннектора
     */
    public void removeConnection(String name, AuthConnector connector) {
        name = ((name == null) ? ((connector == null) ? null : connector.getName()) : name);
        if (name != null) {
            this.connectedUsersSet.remove(name);
            return;
        }
        throw new RuntimeException("Name and connector is null");
    }

    /**
     * Количество подключений на проверке
     *
     * @return количество подключений на проверке
     */
    public int getOnlineOnFilter() {
        return this.connectedUsersSet.size();
    }

    /**
     * Количество пользователей, которые прошли проверку
     *
     * @return количество пользователей, которые прошли проверку
     */
    public int getUsersCount() {
        return this.userCache.size();
    }

    /**
     * Проверяет нужно ли игроку проходить проверку
     *
     * @param userName Имя игрока
     * @param address  InetAddress игрока
     * @return Нужно ли юзеру проходить проверку
     */
    public boolean needCheck(String userName, InetAddress address) {
        final AuthUser authUser = this.userCache.get(userName.toLowerCase());
        return authUser == null || !authUser.getIp().equalsIgnoreCase(address.getHostAddress()) || System.currentTimeMillis() - authUser.getSession() >= SettingsAuth.IMP.SESSION_TIME;
    }

    /**
     * Проверяет, находиться ли игрок на проверке
     *
     * @param name Имя игрока которого нужно искать на проверке
     * @return Находиться ли игрок на проверке
     */
    public boolean isOnChecking(final String name) {
        return this.connectedUsersSet.containsKey(name.toLowerCase());
    }

    public enum CheckStateAuth {
        REGISTER,
        LOGIN,
        SUCCESSFULLY,
        FAILED;
    }
}
