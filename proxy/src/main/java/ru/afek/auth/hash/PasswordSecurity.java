package ru.afek.auth.hash;

import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordSecurity
{
    private static SecureRandom rnd;
    
    public static String createSalt(final int length) throws NoSuchAlgorithmException {
        final byte[] msg = new byte[40];
        PasswordSecurity.rnd.nextBytes(msg);
        final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.reset();
        final byte[] digest = sha1.digest(msg);
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).substring(0, length);
    }
    
    public static String getHash(final String password, final String playerName) throws NoSuchAlgorithmException {
        final HashAlgorithm alg = HashAlgorithm.SHA256;
        EncryptionMethod method;
        try {
            method = (EncryptionMethod)alg.getclass().newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex5) {
            throw new NoSuchAlgorithmException("Problem with this hash algorithm");
        }
        switch (alg) {
            case SHA256: {
                final String salt = createSalt(16);
                if (method == null) {
                    throw new NoSuchAlgorithmException("Unknown hash algorithm");
                }
                return method.getHash(password, salt, playerName);
            }
            default: {
                throw new NoSuchAlgorithmException("Unknown hash algorithm");
            }
        }
    }
    
    public static boolean comparePasswordWithHash(final String password, final String hash, final String playerName) throws NoSuchAlgorithmException {
        final HashAlgorithm algo = HashAlgorithm.SHA256;
        EncryptionMethod method;
        try {
            method = (EncryptionMethod)algo.getclass().newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex5) {
            throw new NoSuchAlgorithmException("Problem with this hash algorithm");
        }
        if (method == null) {
            throw new NoSuchAlgorithmException("Unknown hash algorithm");
        }
        try {
            if (method.comparePassword(hash, password, playerName)) {
                return true;
            }
        }
        catch (Exception ex6) {}
        return false;
    }
    
    static {
        PasswordSecurity.rnd = new SecureRandom();
    }
}
