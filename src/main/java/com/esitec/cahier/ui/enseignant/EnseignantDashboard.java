package com.esitec.cahier.ui.enseignant;

import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.service.AuthService;
import com.esitec.cahier.ui.LoginFrame;
import com.esitec.cahier.ui.utils.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EnseignantDashboard extends JFrame {

    private final Utilisateur enseignant;
    private AjouterSeancePanel ajouterPanel;
    private HistoriqueSeancesPanel historiquePanel;
    private JTabbedPane tabs;

    public EnseignantDashboard(Utilisateur enseignant) {
        this.enseignant = enseignant;
        setTitle("Espace Enseignant — " + enseignant.getNomComplet());
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setOpacity(0f);
        initUI();
        ThemeTransition.register(this, this::refreshTheme);
        Timer t = new Timer(16, null);
        final float[] a = {0f};
        t.addActionListener(e -> {
            a[0] += 0.06f;
            if (a[0] >= 1f) { a[0] = 1f; t.stop(); }
            setOpacity(a[0]);
        });
        t.start();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());

        JPanel top = new JPanel(new BorderLayout());
        top.add(new WindowControls(this, "Espace Enseignant — ESITEC",
            new Color(30, 110, 70)), BorderLayout.NORTH);
        top.add(buildHeader(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        ajouterPanel   = new AjouterSeancePanel(enseignant, this);
        historiquePanel = new HistoriqueSeancesPanel(enseignant);

        tabs = new JTabbedPane() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ThemeManager.getBg());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tabs.setBackground(ThemeManager.getBg());
        tabs.setForeground(ThemeManager.getText());
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("📝 Nouvelle séance", ajouterPanel);
        tabs.addTab("📋 Historique", historiquePanel);
        updateHistoriqueCount();

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                historiquePanel.rafraichir();
                updateHistoriqueCount();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }

    public void updateHistoriqueCount() {
        int count = historiquePanel.getSeanceCount();
        tabs.setTitleAt(1, "📋 Historique" + (count > 0 ? " (" + count + ")" : ""));
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(30, 110, 70));
        h.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // ── Gauche : Avatar + nom + heure connexion ───────────────────
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        AvatarPanel avatar = new AvatarPanel(enseignant.getNomComplet(), 44);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setOpaque(false);

        JLabel lblTitre = new JLabel("Espace Enseignant");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 15));
        lblTitre.setForeground(Color.WHITE);

        JLabel lblNom = new JLabel(enseignant.getNomComplet());
        lblNom.setFont(new Font("Arial", Font.PLAIN, 11));
        lblNom.setForeground(new Color(180, 230, 200));

        String heureConnexion = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel lblSession = new JLabel("🕐 Connecté à " + heureConnexion);
        lblSession.setFont(new Font("Arial", Font.ITALIC, 10));
        lblSession.setForeground(new Color(150, 210, 170));

        info.add(lblTitre);
        info.add(lblNom);
        info.add(lblSession);

        left.add(avatar);
        left.add(info);
        h.add(left, BorderLayout.WEST);

        // ── Droite : Profil + Thème + Déconnexion ────────────────────
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Bouton Profil
        AnimationManager.AnimatedButton btnProfil = new AnimationManager.AnimatedButton(
            "👤 Mon profil",
            new Color(20, 80, 50), new Color(35, 120, 70), new Color(15, 60, 35));
        btnProfil.setPreferredSize(new Dimension(120, 34));
        btnProfil.addActionListener(e -> new ProfilDialog(this).setVisible(true));

        right.add(ThemeManager.createThemeButton(
            () -> ThemeTransition.toggleWithEffect(this)));
        right.add(btnProfil);
        right.add(buildDecoBtn());
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private JButton buildDecoBtn() {
        AnimationManager.AnimatedButton btn = new AnimationManager.AnimatedButton(
            "⏻ Déconnexion",
            new Color(20, 80, 50), new Color(30, 110, 70), new Color(15, 60, 35));
        btn.setPreferredSize(new Dimension(130, 34));
        btn.addActionListener(e -> {
            ThemeTransition.unregister(this);
            AuthService.deconnecter();
            Timer ft = new Timer(16, null);
            final float[] a = {1f};
            ft.addActionListener(ev -> {
                a[0] -= 0.06f;
                if (a[0] <= 0f) { ft.stop(); dispose(); new LoginFrame().setVisible(true); }
                setOpacity(Math.max(0f, a[0]));
            });
            ft.start();
        });
        return btn;
    }

    private void refreshTheme() {
        getContentPane().setBackground(ThemeManager.getBg());
        tabs.setBackground(ThemeManager.getBg());
        tabs.setForeground(ThemeManager.getText());
        ThemeTransition.refreshChildren((JComponent) getContentPane());
        repaint(); revalidate();
    }
}
