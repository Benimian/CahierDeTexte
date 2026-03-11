package com.esitec.cahier.ui;

import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.utils.ThemeManager;
import com.esitec.cahier.ui.utils.Validator;

import javax.swing.*;
import java.awt.*;

public class InscriptionDialog extends JDialog {

    private JTextField nomField, prenomField, emailField, classeField;
    private JPasswordField mdpField, mdpConfirmField;
    private JComboBox<String> roleBox;
    private JLabel lblForcemdp;

    public InscriptionDialog(JFrame parent) {
        super(parent, "Créer un compte", true);
        setSize(450, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getCard());

        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(ThemeManager.ACCENT_BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel titre = new JLabel("✏️ Créer un compte");
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        titre.setForeground(Color.WHITE);
        header.add(titre);
        panel.add(header, BorderLayout.NORTH);

        // ── Formulaire ────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ThemeManager.getCard());
        form.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        nomField       = new JTextField(15);
        prenomField    = new JTextField(15);
        emailField     = new JTextField(15);
        mdpField       = new JPasswordField(15);
        mdpConfirmField = new JPasswordField(15);
        roleBox        = new JComboBox<>(new String[]{"ENSEIGNANT", "RESPONSABLE"});
        classeField    = new JTextField(15);
        lblForcemdp    = new JLabel(" ");
        lblForcemdp.setFont(new Font("Arial", Font.ITALIC, 10));

        // Validations en temps réel
        Validator.addRealTimeValidation(nomField, Validator::isNomValide, "Lettres uniquement (2-50 car.)");
        Validator.addRealTimeValidation(prenomField, Validator::isNomValide, "Lettres uniquement (2-50 car.)");
        Validator.addRealTimeValidation(emailField, Validator::isEmailValide, "Format: nom@domaine.sn");

        // Force du mot de passe en temps réel
        mdpField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { evaluerMdp(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { evaluerMdp(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { evaluerMdp(); }
        });

        // Classe visible seulement si RESPONSABLE
        classeField.setEnabled(false);
        roleBox.addActionListener(e -> {
            boolean isResp = "RESPONSABLE".equals(roleBox.getSelectedItem());
            classeField.setEnabled(isResp);
            if (!isResp) classeField.setText("");
        });

        int row = 0;
        addRow(form, gbc, row++, "Nom :", nomField, "ex: Diallo");
        addRow(form, gbc, row++, "Prénom :", prenomField, "ex: Amadou");
        addRow(form, gbc, row++, "Email :", emailField, "ex: amadou@esitec.sn");
        addRow(form, gbc, row++, "Mot de passe :", mdpField, "minimum 4 caractères");

        gbc.gridx = 1; gbc.gridy = row++;
        form.add(lblForcemdp, gbc);

        addRow(form, gbc, row++, "Confirmer mdp :", mdpConfirmField, "répétez le mot de passe");
        addRow(form, gbc, row++, "Rôle :", roleBox, null);
        addRow(form, gbc, row++, "Classe :", classeField, "obligatoire si Responsable");

        panel.add(form, BorderLayout.CENTER);

        // ── Boutons ───────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(ThemeManager.getCard());

        JButton btnInscrire = ThemeManager.btnSuccess("✅ S'inscrire");
        JButton btnAnnuler  = ThemeManager.btnSecondary("✖ Annuler");
        btnInscrire.setPreferredSize(new Dimension(140, 38));
        btnAnnuler.setPreferredSize(new Dimension(120, 38));

        btnInscrire.addActionListener(e -> inscrire());
        btnAnnuler.addActionListener(e -> dispose());

        btnPanel.add(btnAnnuler);
        btnPanel.add(btnInscrire);
        panel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field, String placeholder) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(ThemeManager.getText());
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        if (placeholder != null && field instanceof JTextField tf) {
            tf.setToolTipText(placeholder);
        }
        p.add(field, gbc);
    }

    private void evaluerMdp() {
        String mdp = new String(mdpField.getPassword());
        if (mdp.isEmpty()) { lblForcemdp.setText(" "); return; }

        int score = 0;
        if (mdp.length() >= 6)                score++;
        if (mdp.matches(".*[A-Z].*"))          score++;
        if (mdp.matches(".*[0-9].*"))          score++;
        if (mdp.matches(".*[^a-zA-Z0-9].*"))   score++;

        switch (score) {
            case 0, 1 -> { lblForcemdp.setText("🔴 Mot de passe faible");   lblForcemdp.setForeground(ThemeManager.ACCENT_RED); }
            case 2    -> { lblForcemdp.setText("🟠 Mot de passe moyen");    lblForcemdp.setForeground(ThemeManager.ACCENT_ORANGE); }
            case 3    -> { lblForcemdp.setText("🟡 Mot de passe bon");      lblForcemdp.setForeground(new Color(200, 180, 0)); }
            case 4    -> { lblForcemdp.setText("🟢 Mot de passe fort");     lblForcemdp.setForeground(ThemeManager.ACCENT_GREEN); }
        }
    }

    private void inscrire() {
        String nom      = nomField.getText().trim();
        String prenom   = prenomField.getText().trim();
        String email    = emailField.getText().trim();
        String mdp      = new String(mdpField.getPassword());
        String mdpConf  = new String(mdpConfirmField.getPassword());
        String role     = (String) roleBox.getSelectedItem();
        String classe   = classeField.getText().trim();

        // Validation globale
        Validator.ValidationResult result = Validator.validerInscription(nom, prenom, email, mdp, mdpConf);

        // Validation classe si responsable
        if ("RESPONSABLE".equals(role) && classe.isEmpty()) {
            var errors = new java.util.ArrayList<>(result.errors());
            errors.add("• Classe obligatoire pour un Responsable");
            result = new Validator.ValidationResult(false, errors);
        }

        if (!result.valid()) {
            Validator.showErrors(this, result.errors());
            return;
        }

        Utilisateur u = new Utilisateur(nom, prenom, email, mdp, role);
        boolean ok = new UtilisateurDAO().ajouter(u);

        if (ok) {
            Validator.showSuccess(this, "Compte créé ! En attente de validation par le chef de département.");
            dispose();
        } else {
            Validator.showErrors(this, java.util.List.of("• Cet email est déjà utilisé. Choisissez-en un autre."));
        }
    }
}
