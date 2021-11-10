package ru.afek.auth.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VerifyCode {

    @Getter
    private String code, email;
}
