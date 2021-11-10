package ru.afek.auth.hash;

import java.util.Random;

public class RandomString
{
    private static final char[] chars;
    private final Random random;
    private final char[] buf;
    
    public RandomString(final int length) {
        this.random = new Random();
        if (length < 1) {
            throw new IllegalArgumentException("length < 1: " + length);
        }
        this.buf = new char[length];
    }
    
    public String nextString() {
        for (int idx = 0; idx < this.buf.length; ++idx) {
            this.buf[idx] = RandomString.chars[this.random.nextInt(RandomString.chars.length)];
        }
        return new String(this.buf);
    }
    
    static {
        chars = new char[36];
        for (int idx = 0; idx < 10; ++idx) {
            RandomString.chars[idx] = (char)(48 + idx);
        }
        for (int idx = 10; idx < 36; ++idx) {
            RandomString.chars[idx] = (char)(97 + idx - 10);
        }
    }
}
