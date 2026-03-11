package com.esitec.cahier.ui.utils;

import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.service.AuthService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ProfilDialog extends JDialog {

    private final Utilisateur user;
    private final UtilisateurDAO dao = new UtilisateurDAO();

    private AvatarPanel avatar;
    private JTextField  nomField, prenomField, emailField;
    private JPasswordField ancienMdp, nouveauMdp, confirmMdp;
    private JLabel lblRole, lblClasse, lblStatus;

    public ProfilDialog(JFrame parent) {
        super(parent, "Mon Profil", true);
        this.user = AuthService.getUtilisateurConnecte();
        setSize(460, 540);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeManager.getCard());
        root.setBorder(BorderFactory.createLineBorder(ThemeManager.ACCENT_BLUE, 2));

        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 60, 114));
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        avatar = new AvatarPanel(user.getNomComplet(), 52);
        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setOpaque(false);
        JLabel lblNom = new JLabel(user.getNomComplet());
        lblNom.setFont(new Font("Arial", Font.BOLD, 14));
        lblNom.setForeground(Color.WHITE);
        lblRole = new JLabel(formatRole(user.getRole()));
        lblRole.setFont(new Font("Arial", Font.PLAIN, 11));
        lblRole.setForeground(new Color(160, 185, 230));
        lblClasse = new JLabel(user.getClasse() != null && !user.getClasse().isBlank()
            ? "Classe : " + user.getClasse() : "");
        lblClasse.setFont(new Font("Arial", Font.ITALIC, 10));
        lblClasse.setForeground(new Color(140, 170, 220));
        info.add(lblNom); info.add(lblRole); info.add(lblClasse);
        left.add(avatar); left.add(info);
        header.add(left, BorderLayout.CENTER);

        JButton btnX = new JButton("✕");
        btnX.setForeground(Color.WHITE);
        btnX.setFont(new Font("Arial", Font.BOLD, 12));
        btnX.setBorderPainted(false); btnX.setFocusPainted(false); btnX.setContentAreaFilled(false);
        btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dispose());
        header.add(btnX, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Corps ─────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(ThemeManager.getCard());
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 10, 20));

        // Section infos personnelles
        body.add(buildSection("👤 Informations personnelles", buildInfosForm()));
        body.add(Box.createVerticalStrut(12));
        body.add(buildSection("🔐 Changer le mot de passe", buildMdpForm()));

        // Status
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);
        body.add(Box.createVerticalStrut(8));
        body.add(lblStatus);

        root.add(new JScrollPane(body), BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        footer.setBackground(ThemeManager.getCard());

        AnimationManager.AnimatedButton btnSave = new AnimationManager.AnimatedButton(
            "💾 Enregistrer", ThemeManager.ACCENT_BLUE,
            new Color(50, 90, 170), new Color(20, 50, 110));
        btnSave.setPreferredSize(new Dimension(160, 38));
        btnSave.addActionListener(e -> sauvegarderInfos());

        AnimationManager.AnimatedButton btnMdp = new AnimationManager.AnimatedButton(
            "🔑 Changer mot de passe", ThemeManager.ACCENT_GREEN,
            new Color(50, 190, 100), new Color(25, 130, 65));
        btnMdp.setPreferredSize(new Dimension(200, 38));
        btnMdp.addActionListener(e -> changerMotDePasse());

        footer.add(btnSave); footer.add(btnMdp);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildInfosForm() {
        JPanel p = new JPanel(new GridLayout(3, 2, 8, 10));
        p.setOpaque(false);
        nomField    = styledField(user.getNom());
        prenomField = styledField(user.getPrenom());
        emailField  = styledField(user.getEmail());
        p.add(lbl("Nom :")); p.add(nomField);
        p.add(lbl("Prénom :")); p.add(prenomField);
        p.add(lbl("Email :")); p.add(emailField);
        return p;
    }

    private JPanel buildMdpForm() {
        JPanel p = new JPanel(new GridLayout(3, 2, 8, 10));
        p.setOpaque(false);
        ancienMdp  = styledPwd();
        nouveauMdp = styledPwd();
        confirmMdp = styledPwd();
        p.add(lbl("Ancien :")); p.add(ancienMdp);
        p.add(lbl("Nouveau :")); p.add(nouveauMdp);
        p.add(lbl("Confirmer :")); p.add(confirmMdp);
        return p;
    }

    private JPanel buildSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1), title);
        tb.setTitleColor(ThemeManager.getText());
        tb.setTitleFont(new Font("Arial", Font.BOLD, 12));
        section.setBorder(BorderFactory.createCompoundBorder(tb,
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    private void sauvegarderInfos() {
        String nom    = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email  = emailField.getText().trim();
        if (nom.isBlank() || prenom.isBlank() || email.isBlank()) {
            setStatus("⚠️ Tous les champs sont requis.", ThemeManager.ACCENT_RED);
            return;
        }
        if (dao.updateProfil(user.getId(), nom, prenom, email)) {
            user.setNom(nom); user.setPrenom(prenom); user.setEmail(email);
            avatar.update(user.getNomComplet());
            setStatus("✅ Profil mis à jour avec succès !", ThemeManager.ACCENT_GREEN);
        } else {
            setStatus("❌ Erreur lors de la mise à jour.", ThemeManager.ACCENT_RED);
        }
    }

    private void changerMotDePasse() {
        String ancien  = new String(ancienMdp.getPassword());
        String nouveau = new String(nouveauMdp.getPassword());
        String confirm = new String(confirmMdp.getPassword());
        if (ancien.isBlank() || nouveau.isBlank()) {
            setStatus("⚠️ Remplissez les champs mot de passe.", ThemeManager.ACCENT_RED);
            return;
        }
        if (!nouveau.equals(confirm)) {
            setStatus("❌ Les mots de passe ne correspondent pas.", ThemeManager.ACCENT_RED);
            AnimationManager.shake(confirmMdp);
            return;
        }
        if (nouveau.length() < 6) {
            setStatus("⚠️ Mot de passe trop court (min. 6 caractères).", ThemeManager.ACCENT_RED);
            return;
        }
        AuthService svc = new AuthService();
        if (svc.changerMotDePasse(user.getId(), ancien, nouveau)) {
            setStatus("✅ Mot de passe changé avec succès !", ThemeManager.ACCENT_GREEN);
            ancienMdp.setText(""); nouveauMdp.setText(""); confirmMdp.setText("");
        } else {
            setStatus("❌ Ancien mot de passe incorrect.", ThemeManager.ACCENT_RED);
            AnimationManager.shake(ancienMdp);
        }
    }

    private void setStatus(String msg, Color color) {
        lblStatus.setText(msg);
        lblStatus.setForeground(color);
        Timer t = new Timer(4000, e -> lblStatus.setText(" "));
        t.setRepeats(false); t.start();
    }

    private JTextField styledField(String val) {
        JTextField f = new JTextField(val);
        f.setBackground(ThemeManager.getBg()); f.setForeground(ThemeManager.getText());
        f.setCaretColor(ThemeManager.getText());
        f.setFont(new Font("Arial", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JPasswordField styledPwd() {
        JPasswordField f = new JPasswordField();
        f.setBackground(ThemeManager.getBg()); f.setForeground(ThemeManager.getText());
        f.setCaretColor(ThemeManager.getText());
        f.setFont(new Font("Arial", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(ThemeManager.getText());
        return l;
    }

    private String formatRole(String role) {
        return switch (role) {
            case "CHEF"        -> "Chef de Département";
            case "ENSEIGNANT"  -> "Enseignant";
            case "RESPONSABLE" -> "Responsable de Classe";
            default -> role;
        };
    }
}
