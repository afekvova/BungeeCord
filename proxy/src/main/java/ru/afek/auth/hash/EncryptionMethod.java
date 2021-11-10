package ru.afek.auth.hash;

import java.security.NoSuchAlgorithmException;

public interface EncryptionMethod
{
    String getHash(final String p0, final String p1, final String p2) throws NoSuchAlgorithmException;
    
    boolean comparePassword(final String p0, final String p1, final String p2) throws NoSuchAlgorithmException;
}
