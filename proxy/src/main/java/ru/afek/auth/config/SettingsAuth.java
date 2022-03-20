package ru.afek.auth.config;

import ru.afek.bungeecord.Config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SettingsAuth extends Config {

    @Ignore
    public static final SettingsAuth IMP = new SettingsAuth();
    @Final
    public final String HELP = "vk.com/afekvova | afek.ru | t.me/afekvova";

    public String PREFIX = "&f[&6Auth&f]";
    @Comment("Сколько времени есть у игрока чтобы пройти защиту. В миллисекундах. 1 сек = 1000")
    public int TIME_OUT = 120000;
    @Comment("Сколько времени у игрока будет действовать сессия. В миллисекундах. 1 сек = 1000")
    public int SESSION_TIME = 1800000;

    //    @Comment("Сколько аккаунт может иметь пользователь")
    //    public int USER_COUNT = 5;
    //    public String USER_COUNT_MSG = "&cУ вас нее может быть больше 5 аккаунтов.";
    @Comment("Сообщение когда игрок не смог ввести пароль 3 раза")
    public String PLAYER_BANNED_BY_TRY = "&cВы забанены на 10 минут!";
    public String PLAYER_NAME_ERROR = "&cВ нике есть недопустимые символы \\n§cРазрешено использовать только цифры и символы латинского алфавита.";

    @Create
    public LOGIN LOGIN;

    @Comment({"Не используйте '\\n', используйте %nl%"})
    public static class LOGIN {
        public String USAGE_LOG = "%prefix% &fАвторизируйтесь - &6/login&f [&6пароль&f]&6.";
        public String WRONG_PWD = "%prefix% &fНеправильный пароль!";
        public String LOGIN_SUCCESS = "%prefix% &fВы успешно вошли!";
        public String TIMEOUT = "&cВремя авторизации истекло.";
        public String NOT_REGISTERED = "%prefix% &fВы ещё не зарегистрированы!";
        public String ALREADY_LOG = "%prefix% &fВы уже авторизованы, играйте!";
        public String TRYING = "%prefix% &fУ вас осталасть %try% %padezh%.";
        public String VALID_SESSION = "%prefix% &fВы автоматически авторизовались";
        public String EMAIL_RECOVERY = "%prefix% &fЗабыли пароль? Восстановить пароль: /recovery <email>";
        public String EMAIL_RECOVERY_ACTION_BAR = "&6Восстановить пароль: /recovery <email>";
//        public List<String> EMAIL_ADD_MSG = Arrays.asList("__________________________________",
//                "Здраствуйте привяжите то-то-то",
//                "мы беспокоимся о вашей безопасноти",
//                "и т.д",
//                "________________________________");
    }

    @Create
    public WHITELIST WHITELIST;

    @Comment("Настройки для whitelist")
    public static class WHITELIST {
        public boolean ENABLE = false;
        public List<String> KICK_MESSAGE = Arrays.asList("____________________________",
                "На сервере ведутся тех.работы",
                "Наш сайт: ",
                "Наша группа:",
                "_____________________________");
    }

    @Create
    public EMAIL EMAIL;

    @Comment("Настройки для email")
    public static class EMAIL {
        public String HOST = "localhost";
        public String PORT = "25";
        public String USER = "root";
        public String PASSWORD = "password";
        public int PASSWORD_LENGHT = 6;
    }

    @Create
    public EMAIL_COMMAND EMAIL_COMMAND;

    @Comment("Настройки для email commands")
    public static class EMAIL_COMMAND {
        public List<String> EMAIL_HELP = Arrays.asList("&aПодкоманды для email:",
                "&7/email add <ваша электронная почта> <еще раз> &f- Привязать эл. почту к вашему аккаунту",
                "&7/email change <старая эл. почта> <новая эл. почта> &f- Сменить эл. почту на вашем аккаунте",
                "&7/email verify <код подтверждения> &f- Подтвердить привязку или смену эл. почты",
                "&7/recovery <email> &f- Выслать на почту новый пароль.");
    }

    @Create
    public EMAIL_RECOVERY_COMMAND EMAIL_RECOVERY_COMMAND;

    @Comment("Настройки для email recovery")
    public static class EMAIL_RECOVERY_COMMAND {
        public String EMAIL_RECOVERY = "&7/recovery <email> &f- Выслать на почту новый пароль.";
        public String NO_EMAIL = "&7У вас не привязана почта!";
        public String SUBJECT = "Обновление пароля";
        public String NEW_EMAIL_MSG = "&cМы отправили новый пароль вам на почту. Команда для смены пароля /changepassword <старый пароль> <новый>";
        public String NEW_PASSWORD_TEXT_EMAIL = "Дорогой <playername>, <br/><br/> Это твой новый пароль на сервере ServerName<br/><br/><br/><br/>Твой новый пароль - <generatedpass><br/><br/>Не забывайте менять пароль после входа в систему! <br/> /changepassword <generatedpass> <новый пароль>";
    }

    @Create
    public CHANGE_PASSWORD_COMMAND CHANGE_PASSWORD_COMMAND;

    @Comment("Настройки для change password")
    public static class CHANGE_PASSWORD_COMMAND {
        public String CHANGE_PASSWORD = "&7/changepassword [старый пароль] [новый пароль] &f- Сменить пароль на новый.";
        public String MATCH_ERROR_PWD = "%prefix% &fНеверный старый пароль.";
        public String WRONG_CHARS = "%prefix% &fНовый пароль содержит запрещённые символы. Разрешённые: &6%valid_chars%";
        public String WRONG_LENGHT = "%prefix% &fНовый пароль слишком длинный/короткий.";
        public String CHANGE_PASSWORD_MSG = "%prefix% &fВы успешно сменили пароль на новый!";
    }

    @Create
    public EMAIL_ADD_COMMAND EMAIL_ADD_COMMAND;

    @Comment("Настройки для email add")
    public static class EMAIL_ADD_COMMAND {
        public String EMAIL_ADD = "&7/email add <ваша электронная почта> <еще раз> &f- Привязать эл. почту к вашему аккаунту";
        public String INVALID_EMAIL = "%prefix% &fЭто не эл. почта!";
        public String MATCH_ERROR_EMAIL = "%prefix% &fЭл. почты не совпадают.";
        public String HAVE_EMAIL = "%prefix% &fУ вас уже есть эл. почта. Используйте: &7/email change <старая эл. почта> <новая эл. почта>";
        public String EMAIL_ADD_MSG = "%prefix% &fВы привязали новую почту к своему аккаунту. Мы отправили на нее код подтверждения.";
        public String EMAIL_VERIFY_MSG = "Дорогой <playername>, <br/><br/> Это ваш проверочный код для сервера ServerName<br/><br/><br/><br/>Проверочный код - <verifycode><br/>/email verify <код>";
    }

    @Create
    public EMAIL_CHANGE_COMMAND EMAIL_CHANGE_COMMAND;

    @Comment("Настройки для email change")
    public static class EMAIL_CHANGE_COMMAND {
        public String EMAIL_CHANGE = "&7/email change <старая электронная почта> <новая электронная почта> &f- Сменить эл. почту на вашем аккаунте";
        public String INVALID_NEW_EMAIL = "%prefix% &fЭто не эл. почта!";
        public String INVALID_OLD_EMAIL = "%prefix% &fНекорректно введена старая почта!";
        public String NO_EMAIL = "%prefix% &fУ вас нету эл. почты. Используйте: &7/email add <ваша электронная почта> <еще раз>";
        public String EMAIL_CHANGE_MSG = "%prefix% &fВы сменили старую почту на новую. Мы отправили на новую почту код подтверждения.";
        public String EMAIL_VERIFY_MSG = "Дорогой <playername>, <br/><br/> Это ваш проверочный код для сервера ServerName<br/><br/><br/><br/>Проверочный код - <verifycode><br/>/email verify <код>";
    }

    @Create
    public EMAIL_VERIFY_COMMAND EMAIL_VERIFY_COMMAND;

    @Comment("Настройки для email verify")
    public static class EMAIL_VERIFY_COMMAND {
        public String EMAIL_VERIFY = "&7/email verify <код подтверждения> &f- Подтвердить привязку или смену эл. почты";
        public String NO_REQUEST = "&cВ данный момент ничего подтверждать не нужно!";
        public String INVALID_CODE = "&cВремя действия кода истекло.";
        public String SUBJECT = "Код подтверждения";
        public String EMAIL_VERIFY_MSG = "&aВы подтвердили почту. Удачной игры!";
    }

    @Create
    public REGISTER REGISTER;

    public static class REGISTER {
        public String USAGE_REG = "%prefix% &fЗарегистрируйтесь - &6/register &f[&6пароль&f] [&6повторите пароль&f]&6.";
        public String REG_SUCCESS = "%prefix% &fВы успешно зарегистрировались!";
        public String MATCH_ERROR_PWD = "%prefix% &fПароли не совпадают.";
        public String WRONG_CHARS = "%prefix% &fПароль содержит запрещённые символы. Разрешённые: &6%valid_chars%";
        public String WRONG_LENGHT = "%prefix% &fПароль слишком длинный/короткий.";
        public String ALREADY_REG = "%prefix% &fВы уже зарегистрировались, играйте!";
        public String ALREADY_REG_NOT = "%prefix% &fВы уже зарегистрировались, авторизируйтесь!";
    }

    @Create
    public LOGOUT LOGOUT;

    public static class LOGOUT {
        public String USAGE_LOG = "%prefix% &fВыйти - &6/logout.";
        public String LOG_SUCCESS = "&cВы успешно вышли!";
    }

    @Create
    public ERROR ERROR;

    public static class ERROR {
        public String DENIED_CMD = "%prefix% &fНеобходимо авторизоваться для использования этой команды!";
        public String DENIED_CHAT = "%prefix% &fНеобходимо авторизоваться, чтобы писать в чат!";
    }

    public void reload(final File file) {
        this.load(file);
        this.save(file);
    }
}
