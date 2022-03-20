package ru.afek.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuthUser {

    String name, password, ip;
    long session;
    String email;

    public void logout() {
        this.session = -1L;
        this.ip = "0.0.0.0";
    }
}
