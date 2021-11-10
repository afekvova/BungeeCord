package ru.afek.auth.hash;

public enum HashAlgorithm
{
    SHA256((Class<?>)SHA256.class);
    
    Class<?> classe;
    
    private HashAlgorithm(final Class<?> classe) {
        this.classe = classe;
    }
    
    public Class<?> getclass() {
        return this.classe;
    }
}
