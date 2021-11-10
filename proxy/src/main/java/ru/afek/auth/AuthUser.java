package ru.afek.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class AuthUser {

    @Getter
    private String name;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String ip;
    @Getter
    @Setter
    private long session;
    @Getter
    @Setter
    private String email;

    public void logout() {
        this.session = -1L;
        this.ip = "0.0.0.0";
    }
}
