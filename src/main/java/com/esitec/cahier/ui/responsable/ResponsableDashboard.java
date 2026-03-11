package com.esitec.cahier.ui.responsable;

import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.service.AuthService;
import com.esitec.cahier.ui.LoginFrame;
import com.esitec.cahier.ui.utils.*;

import javax.swing.*;
import java.awt.*;

public class ResponsableDashboard extends JFrame {

    private final Utilisateur responsable;

    public ResponsableDashboard(Utilisateur responsable) {
        this.responsable = responsable;
        setTitle("Espace Responsable — " + responsable.getNomComplet());
        setSize(820, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setOpacity(0f);
        initUI();
        ThemeTransition.register(this, this::refreshTheme);
        Timer t = new Timer(16, null);
        final float[] a = {0f};
        t.addActionListener(e -> { a[0] += 0.06f; if (a[0] >= 1f) { a[0] = 1f; t.stop(); } setOpacity(a[0]); });
        t.start();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());

        JPanel top = new JPanel(new BorderLayout());
        top.add(new WindowControls(this, "Responsable de Classe — ESITEC", new Color(100, 40, 140)), BorderLayout.NORTH);
        top.add(buildHeader(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("✅ Validation des séances", new ValidationPanel(responsable));
        tabs.addTab("📈 Avancement", new AvancementPanel(responsable));
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(100, 40, 140));
        h.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel("👤");
        logo.setFont(new Font("Arial", Font.PLAIN, 22));
        JPanel tb = new JPanel(new GridLayout(2, 1));
        tb.setOpaque(false);
        JLabel t1 = new JLabel("Responsable de Classe");
        t1.setFont(new Font("Arial", Font.BOLD, 15));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Connecté : " + responsable.getNomComplet());
        t2.setFont(new Font("Arial", Font.PLAIN, 11));
        t2.setForeground(new Color(210, 180, 240));
        tb.add(t1); tb.add(t2);
        left.add(logo); left.add(tb);
        h.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(ThemeManager.createThemeButton(() -> ThemeTransition.toggleWithEffect(this)));

        AnimationManager.AnimatedButton btnDeco = new AnimationManager.AnimatedButton(
            "Déconnexion", new Color(70, 25, 100), new Color(100, 40, 140), new Color(50, 15, 75));
        btnDeco.setPreferredSize(new Dimension(120, 34));
        btnDeco.addActionListener(e -> {
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
        right.add(btnDeco);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private void refreshTheme() {
        getContentPane().setBackground(ThemeManager.getBg());
        ThemeTransition.refreshChildren((JComponent) getContentPane());
        repaint(); revalidate();
    }
}
