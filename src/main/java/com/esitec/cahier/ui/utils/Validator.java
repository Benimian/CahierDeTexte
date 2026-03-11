package com.esitec.cahier.ui.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Validator {

    // ── Patterns ──────────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN   = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern DATE_PATTERN    = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern HEURE_PATTERN   = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final Pattern NOM_PATTERN     = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]{2,50}$");

    // ── Bordures feedback ─────────────────────────────────────────────
    private static final Border BORDER_OK    = BorderFactory.createLineBorder(ThemeManager.ACCENT_GREEN, 1, true);
    private static final Border BORDER_ERROR = BorderFactory.createLineBorder(ThemeManager.ACCENT_RED, 2, true);
    private static final Border BORDER_WARN  = BorderFactory.createLineBorder(ThemeManager.ACCENT_ORANGE, 1, true);
    private static final Border BORDER_INNER = BorderFactory.createEmptyBorder(4, 8, 4, 8);

    // ── Résultat de validation ────────────────────────────────────────
    public record ValidationResult(boolean valid, List<String> errors) {
        public static ValidationResult ok()                    { return new ValidationResult(true, List.of()); }
        public static ValidationResult error(String... msgs)   { return new ValidationResult(false, List.of(msgs)); }
    }

    // ════════════════════════════════════════════════════════════════════
    // MÉTHODES DE VALIDATION INDIVIDUELLES
    // ════════════════════════════════════════════════════════════════════

    public static boolean isEmailValide(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isDateValide(String date) {
        if (date == null || !DATE_PATTERN.matcher(date.trim()).matches()) return false;
        try {
            String[] parts = date.split("-");
            int mois = Integer.parseInt(parts[1]);
            int jour  = Integer.parseInt(parts[2]);
            return mois >= 1 && mois <= 12 && jour >= 1 && jour <= 31;
        } catch (Exception e) { return false; }
    }

    public static boolean isHeureValide(String heure) {
        return heure != null && HEURE_PATTERN.matcher(heure.trim()).matches();
    }

    public static boolean isNomValide(String nom) {
        return nom != null && NOM_PATTERN.matcher(nom.trim()).matches();
    }

    public static boolean isMotDePasseValide(String mdp) {
        return mdp != null && mdp.length() >= 4;
    }

    public static boolean isDureeValide(String duree) {
        try {
            int d = Integer.parseInt(duree.trim());
            return d > 0 && d <= 12;
        } catch (Exception e) { return false; }
    }

    public static boolean isVolumeHoraireValide(String volume) {
        try {
            int v = Integer.parseInt(volume.trim());
            return v > 0 && v <= 500;
        } catch (Exception e) { return false; }
    }

    public static boolean isContenuValide(String contenu) {
        return contenu != null && contenu.trim().length() >= 5;
    }

    public static boolean isNotEmpty(String val) {
        return val != null && !val.trim().isEmpty();
    }

    // ════════════════════════════════════════════════════════════════════
    // VALIDATION COMPLÈTE PAR FORMULAIRE
    // ════════════════════════════════════════════════════════════════════

    public static ValidationResult validerEnseignant(String nom, String prenom, String email, String mdp) {
        List<String> errors = new ArrayList<>();
        if (!isNomValide(nom))              errors.add("• Nom invalide (2-50 lettres, pas de chiffres)");
        if (!isNomValide(prenom))           errors.add("• Prénom invalide (2-50 lettres, pas de chiffres)");
        if (!isEmailValide(email))          errors.add("• Email invalide (ex: nom@domaine.sn)");
        if (!isMotDePasseValide(mdp))       errors.add("• Mot de passe trop court (minimum 4 caractères)");
        return errors.isEmpty() ? ValidationResult.ok() : new ValidationResult(false, errors);
    }

    public static ValidationResult validerSeance(String date, String heure, String duree, String contenu) {
        List<String> errors = new ArrayList<>();
        if (!isDateValide(date))            errors.add("• Date invalide (format: AAAA-MM-JJ, ex: 2026-03-17)");
        if (!isHeureValide(heure))          errors.add("• Heure invalide (format: HH:MM, ex: 08:30)");
        if (!isDureeValide(duree))          errors.add("• Durée invalide (entre 1 et 12 heures)");
        if (!isContenuValide(contenu))      errors.add("• Contenu trop court (minimum 5 caractères)");
        return errors.isEmpty() ? ValidationResult.ok() : new ValidationResult(false, errors);
    }

    public static ValidationResult validerCours(String intitule, String classe, String volume) {
        List<String> errors = new ArrayList<>();
        if (!isNotEmpty(intitule) || intitule.length() < 3)
                                            errors.add("• Intitulé trop court (minimum 3 caractères)");
        if (!isNotEmpty(classe))            errors.add("• Classe obligatoire");
        if (!isVolumeHoraireValide(volume)) errors.add("• Volume horaire invalide (entre 1 et 500 heures)");
        return errors.isEmpty() ? ValidationResult.ok() : new ValidationResult(false, errors);
    }

    public static ValidationResult validerInscription(String nom, String prenom, String email, String mdp, String mdpConfirm) {
        List<String> errors = new ArrayList<>();
        if (!isNomValide(nom))              errors.add("• Nom invalide (lettres uniquement)");
        if (!isNomValide(prenom))           errors.add("• Prénom invalide (lettres uniquement)");
        if (!isEmailValide(email))          errors.add("• Email invalide (ex: prenom.nom@esitec.sn)");
        if (!isMotDePasseValide(mdp))       errors.add("• Mot de passe trop court (minimum 4 caractères)");
        if (!mdp.equals(mdpConfirm))        errors.add("• Les mots de passe ne correspondent pas");
        return errors.isEmpty() ? ValidationResult.ok() : new ValidationResult(false, errors);
    }

    // ════════════════════════════════════════════════════════════════════
    // FEEDBACK VISUEL SUR LES CHAMPS
    // ════════════════════════════════════════════════════════════════════

    /** Marque un champ en rouge avec message d'erreur */
    public static void markError(JTextField field, String tooltip) {
        field.setBorder(BorderFactory.createCompoundBorder(BORDER_ERROR, BORDER_INNER));
        field.setToolTipText("❌ " + tooltip);
        field.setBackground(new Color(255, 235, 235));
    }

    /** Marque un champ en vert (valide) */
    public static void markOk(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(BORDER_OK, BORDER_INNER));
        field.setToolTipText("✅ Valide");
        field.setBackground(new Color(235, 255, 240));
    }

    /** Remet un champ à son état neutre */
    public static void markNeutral(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true), BORDER_INNER));
        field.setToolTipText(null);
        field.setBackground(ThemeManager.getBg());
    }

    /** Validation en temps réel sur un champ */
    public static void addRealTimeValidation(JTextField field, java.util.function.Predicate<String> check, String errorMsg) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { validate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { validate(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            void validate() {
                String val = field.getText().trim();
                if (val.isEmpty())         markNeutral(field);
                else if (check.test(val))  markOk(field);
                else                       markError(field, errorMsg);
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════
    // AFFICHAGE DES ERREURS
    // ════════════════════════════════════════════════════════════════════

    /** Affiche un dialog d'erreurs stylisé */
    public static void showErrors(java.awt.Component parent, List<String> errors) {
        StringBuilder sb = new StringBuilder("<html><body style='font-family:Arial;'>");
        sb.append("<b style='color:#E74C3C;'>⚠️ Erreurs de saisie :</b><br><br>");
        for (String e : errors) {
            sb.append("<span style='color:#C0392B;'>").append(e).append("</span><br>");
        }
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(parent, sb.toString(), "Validation", JOptionPane.ERROR_MESSAGE);
    }

    /** Affiche un message de succès stylisé */
    public static void showSuccess(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
            "<html><b style='color:#27AE60;'>✅ " + message + "</b></html>",
            "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
}
