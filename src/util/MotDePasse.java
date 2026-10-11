package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hachage des mots de passe en hexadecimal SHA-256 (64 caracteres).
 * Remplace le stockage en clair dans la table {@code utilisateur}.
 */
public final class MotDePasse {

    private MotDePasse() {
    }

    /** Hache le mot de passe en clair en hexadécimal SHA-256. */
    public static String hacher(String clair) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] octets = md.digest((clair == null ? "" : clair)
                    .getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : octets) {
                hex.append(String.format("%02x", b & 0xFF));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 indisponible", e);
        }
    }

    /** Vérifie que le mot de passe en clair correspond au hachage stocké. */
    public static boolean verifier(String clair, String hash) {
        if (clair == null || hash == null) {
            return false;
        }
        return hash.equalsIgnoreCase(hacher(clair));
    }

    /** Vrai si la valeur stockée est un hachage SHA-256 (64 caractères hexadécimaux). */
    public static boolean estHache(String valeur) {
        if (valeur == null || valeur.length() != 64) {
            return false;
        }
        for (int i = 0; i < valeur.length(); i++) {
            char c = valeur.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }
}