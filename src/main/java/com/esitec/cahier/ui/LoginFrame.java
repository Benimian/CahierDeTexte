package com.esitec.cahier.ui;

import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.service.AuthService;
import com.esitec.cahier.ui.chef.ChefDashboard;
import com.esitec.cahier.ui.enseignant.EnseignantDashboard;
import com.esitec.cahier.ui.responsable.ResponsableDashboard;
import com.esitec.cahier.ui.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private final AuthService authService = new AuthService();
    private JPanel formPanel;
    private Point dragOrigin;

    public LoginFrame() {
        setTitle("Cahier de Texte Numérique — ESITEC");
        setSize(440, 400);
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setOpacity(0f);
        initUI();
        Timer t = new Timer(16, null);
        final float[] a = {0f};
        t.addActionListener(e -> {
            a[0] += 0.05f;
            if (a[0] >= 1f) { a[0] = 1f; t.stop(); }
            setOpacity(a[0]);
        });
        t.start();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 246, 250));
        root.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 114), 1));

        // ── Barre de titre custom ─────────────────────────────────────
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 30, 75));
        titleBar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 8));
        titleBar.setPreferredSize(new Dimension(0, 34));

        JLabel lblWin = new JLabel("📚 ESITEC — Cahier de Texte Numérique");
        lblWin.setFont(new Font("Arial", Font.BOLD, 11));
        lblWin.setForeground(new Color(160, 185, 230));
        titleBar.add(lblWin, BorderLayout.WEST);

        JButton btnClose = new JButton("✕") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(220, 50, 40) : new Color(200, 40, 30));
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Arial", Font.BOLD, 11));
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setPreferredSize(new Dimension(24, 24));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> System.exit(0));
        titleBar.add(btnClose, BorderLayout.EAST);

        // Drag
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragOrigin = e.getPoint(); }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - dragOrigin.x, loc.y + e.getY() - dragOrigin.y);
            }
        });
        root.add(titleBar, BorderLayout.NORTH);

        // ── Header bleu fixe (toujours clair) ────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 60, 114));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel hl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hl.setOpaque(false);
        JLabel logo = new JLabel("📚");
        logo.setFont(new Font("Arial", Font.PLAIN, 28));
        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titre = new JLabel("");
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        titre.setForeground(Color.WHITE);
        JLabel sousTitre = new JLabel("Connectez-vous à votre espace");
        sousTitre.setFont(new Font("Arial", Font.ITALIC, 11));
        sousTitre.setForeground(new Color(160, 185, 230));
        titleBlock.add(titre);
        titleBlock.add(sousTitre);
        hl.add(logo);
        hl.add(titleBlock);
        header.add(hl, BorderLayout.CENTER);
        root.add(header, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() ->
            AnimationManager.typeWriter(titre, "ESITEC — Cahier de Texte", 40));

        // ── Formulaire (fond blanc fixe) ──────────────────────────────
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(22, 45, 20, 45));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        emailField    = new JTextField(15);
        passwordField = new JPasswordField(15);
        styleField(emailField);
        styleField(passwordField);
        Validator.addRealTimeValidation(emailField, Validator::isEmailValide, "Format: nom@domaine.sn");

        JLabel lblEmail = makeLabel("Email :");
        JLabel lblMdp   = makeLabel("Mot de passe :");

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(lblEmail, gbc);
        gbc.gridx = 1; formPanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(lblMdp, gbc);
        gbc.gridx = 1; formPanel.add(passwordField, gbc);

        // ── Bouton connexion ──────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 5, 6, 5);
        AnimationManager.AnimatedButton btnConnexion = new AnimationManager.AnimatedButton(
            "🔐 Se connecter",
            new Color(30, 60, 114), new Color(40, 80, 150), new Color(20, 40, 90));
        btnConnexion.setPreferredSize(new Dimension(0, 42));
        formPanel.add(btnConnexion, gbc);

        // ── Bouton inscription ────────────────────────────────────────
        gbc.gridy = 3; gbc.insets = new Insets(4, 5, 8, 5);
        AnimationManager.AnimatedButton btnInscription = new AnimationManager.AnimatedButton(
            "✏️ Créer un compte",
            new Color(39, 174, 96), new Color(50, 200, 110), new Color(28, 140, 75));
        btnInscription.setPreferredSize(new Dimension(0, 38));
        formPanel.add(btnInscription, gbc);

        root.add(formPanel, BorderLayout.SOUTH);
        setContentPane(root);

        btnConnexion.addActionListener(e -> seConnecter());
        passwordField.addActionListener(e -> seConnecter());
        btnInscription.addActionListener(e -> new InscriptionDialog(this).setVisible(true));
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(245, 246, 250));
        f.setForeground(new Color(30, 30, 30));
        f.setCaretColor(new Color(30, 30, 30));
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 60, 114), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                AnimationManager.animateBgColor(f, new Color(245, 246, 250), new Color(230, 240, 255), 200);
            }
            public void focusLost(FocusEvent e) {
                AnimationManager.animateBgColor(f, new Color(230, 240, 255), new Color(245, 246, 250), 200);
            }
        });
    }

    private JLabel makeLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(new Color(30, 30, 30));
        return l;
    }

    private void seConnecter() {
        String email = emailField.getText().trim();
        String mdp   = new String(passwordField.getPassword());

        if (email.isEmpty() || mdp.isEmpty()) {
            if (email.isEmpty()) AnimationManager.shake(emailField);
            if (mdp.isEmpty())   AnimationManager.shake(passwordField);
            AnimationManager.pulse(formPanel, Color.WHITE, new Color(255, 230, 230), 2);
            return;
        }

        Utilisateur u = authService.connecter(email, mdp);
        if (u == null) {
            AnimationManager.shake(emailField);
            AnimationManager.shake(passwordField);
            AnimationManager.pulse(formPanel, Color.WHITE, new Color(255, 220, 220), 3);
            Validator.showErrors(this, java.util.List.of(
                "• Email ou mot de passe incorrect.",
                "• Ou compte non encore validé."));
            return;
        }

        // Fade out
        Timer fo = new Timer(16, null);
        final float[] a = {1f};
        fo.addActionListener(e -> {
            a[0] -= 0.06f;
            if (a[0] <= 0f) {
                fo.stop(); dispose();
                switch (u.getRole()) {
                    case "CHEF"        -> new ChefDashboard(u).setVisible(true);
                    case "ENSEIGNANT"  -> new EnseignantDashboard(u).setVisible(true);
                    case "RESPONSABLE" -> new ResponsableDashboard(u).setVisible(true);
                }
            }
            setOpacity(Math.max(0f, a[0]));
        });
        fo.start();
    }
}
