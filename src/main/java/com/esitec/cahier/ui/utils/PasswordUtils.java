package com.esitec.cahier.ui.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    private static final int ROUNDS = 12;

    /** Hash un mot de passe en clair */
    public static String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(ROUNDS));
    }

    /** Vérifie un mot de passe contre son hash */
    public static boolean verify(String plaintext, String hashed) {
        try {
            // Compatibilité : si le hash ne commence pas par $2a$ c'est l'ancien plaintext
            if (!hashed.startsWith("$2a$") && !hashed.startsWith("$2b$")) {
                return plaintext.equals(hashed);
            }
            return BCrypt.checkpw(plaintext, hashed);
        } catch (Exception e) {
            return false;
        }
    }

    /** Détermine si un hash doit être migré (ancien mot de passe en clair) */
    public static boolean needsMigration(String stored) {
        return !stored.startsWith("$2a$") && !stored.startsWith("$2b$");
    }
}
