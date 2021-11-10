package ru.afek.auth.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import ru.afek.auth.config.SettingsAuth;

public class EmailSystem {

    private final String mail, password, sender, host;
    private final int port;

    public EmailSystem() {
        this.mail = SettingsAuth.IMP.EMAIL.USER;
        this.password = SettingsAuth.IMP.EMAIL.PASSWORD;
        this.sender = SettingsAuth.IMP.EMAIL.USER;
        this.host = SettingsAuth.IMP.EMAIL.HOST;
        this.port = Integer.parseInt(SettingsAuth.IMP.EMAIL.PORT);
    }

    private void sendEmailMsg(String receiver, String text, String subject) {
        Properties props = new Properties();

        // putting props
        props.put("mail.smtp.host", this.host);
        props.put("mail.smtp.socketFactory.port", this.port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", this.port);
        props.put("mail.smtp.starttls.enable", true);
        // session
        final Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailSystem.this.mail, EmailSystem.this.password);
            }
        });

        try {
            MimeMessage ignore = new MimeMessage(session);

            try {
                ignore.setFrom(new InternetAddress(this.mail, this.sender));
            } catch (UnsupportedEncodingException useException) {
                ignore.setFrom(new InternetAddress(this.mail));
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            ignore.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
            ignore.setSubject(subject, "UTF-8");
            ignore.setSentDate(new Date());
            ignore.setContent(text, "text/html; charset=UTF-8");

            // replace bungee abstract class
            CompletableFuture.runAsync(() -> {
                try {
                    Transport.send(ignore);
                } catch (MessagingException messagingException) {
                    messagingException.printStackTrace();
                }
            });
        } catch (MessagingException runtimeException) {
            throw new RuntimeException(runtimeException);
        }
    }

    public void sendVerifyCodeEmailMessage(String name, String email, String verify) {
        this.sendEmailMsg(email, SettingsAuth.IMP.EMAIL_ADD_COMMAND.EMAIL_VERIFY_MSG.replace("<playername>", name).replace("<verifycode>", verify), SettingsAuth.IMP.EMAIL_VERIFY_COMMAND.SUBJECT);
    }

    public void sendNewPasswordEmailMessage(String name, String email, String password) {
        this.sendEmailMsg(email, SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.NEW_PASSWORD_TEXT_EMAIL.replace("<playername>", name).replace("<generatedpass>", password), SettingsAuth.IMP.EMAIL_RECOVERY_COMMAND.SUBJECT);
    }
}