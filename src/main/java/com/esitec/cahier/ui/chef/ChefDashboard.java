package com.esitec.cahier.ui.chef;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.*;
import com.esitec.cahier.service.AuthService;
import com.esitec.cahier.service.PdfService;
import com.esitec.cahier.ui.LoginFrame;
import com.esitec.cahier.ui.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ChefDashboard extends JFrame {

    private final Utilisateur chef;

    public ChefDashboard(Utilisateur chef) {
        this.chef = chef;
        setTitle("Chef de Département — " + chef.getNomComplet());
        setSize(900, 680);
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
        top.add(new WindowControls(this, "Chef de Département — ESITEC", new Color(20, 40, 80)), BorderLayout.NORTH);
        top.add(buildHeader(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.setBackground(ThemeManager.getBg());
        tabs.addTab("👥 Gestion Utilisateurs", new GestionEnseignants(chef));
        tabs.addTab("📊 Statistiques", new StatistiquesPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(20, 40, 80));
        h.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel("📚");
        logo.setFont(new Font("Arial", Font.PLAIN, 22));
        JPanel tb = new JPanel(new GridLayout(2, 1));
        tb.setOpaque(false);
        JLabel t1 = new JLabel("Chef de Département");
        t1.setFont(new Font("Arial", Font.BOLD, 15));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Connecté : " + chef.getNomComplet());
        t2.setFont(new Font("Arial", Font.PLAIN, 11));
        t2.setForeground(new Color(160, 185, 230));
        tb.add(t1); tb.add(t2);
        left.add(logo); left.add(tb);
        h.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(ThemeManager.createThemeButton(() -> ThemeTransition.toggleWithEffect(this)));
        right.add(buildPdfButton());
        right.add(buildDecoButton());
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private JButton buildPdfButton() {
        AnimationManager.AnimatedButton btn = new AnimationManager.AnimatedButton(
            "📄 Générer PDF",
            new Color(200, 40, 30), new Color(220, 60, 45), new Color(170, 25, 15));
        btn.setPreferredSize(new Dimension(150, 34));
        btn.addActionListener(e -> ouvrirDialogPdf());
        return btn;
    }

    private JButton buildDecoButton() {
        AnimationManager.AnimatedButton btn = new AnimationManager.AnimatedButton(
            "Déconnexion",
            new Color(60, 70, 100), new Color(80, 90, 130), new Color(40, 50, 80));
        btn.setPreferredSize(new Dimension(120, 34));
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
        ThemeTransition.refreshChildren((JComponent) getContentPane());
        repaint(); revalidate();
    }

    private void ouvrirDialogPdf() {
        List<Cours> cours = new CoursDAO().listerTous();
        if (cours.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Aucun cours disponible.", "Aucun cours", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog(this, "Générer PDF", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(ThemeManager.getCard());

        JPanel dh = new JPanel(new BorderLayout());
        dh.setBackground(new Color(200, 40, 30));
        dh.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel dt = new JLabel("📄  Générer une Fiche de Suivi Pédagogique");
        dt.setFont(new Font("Arial", Font.BOLD, 14));
        dt.setForeground(Color.WHITE);
        dh.add(dt);
        main.add(dh, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(ThemeManager.getCard());
        body.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lc = new JLabel("📚 Cours :");
        lc.setFont(new Font("Arial", Font.BOLD, 13));
        lc.setForeground(ThemeManager.getText());
        body.add(lc, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JComboBox<Cours> combo = new JComboBox<>(cours.toArray(new Cours[0]));
        combo.setBackground(ThemeManager.getBg());
        body.add(combo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel ld = new JLabel("📁 Destination :");
        ld.setFont(new Font("Arial", Font.BOLD, 13));
        ld.setForeground(ThemeManager.getText());
        body.add(ld, gbc);
        JPanel destPanel = new JPanel(new BorderLayout(6, 0));
        destPanel.setOpaque(false);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField destField = new JTextField(System.getProperty("user.home") + "/Desktop");
        destField.setBackground(ThemeManager.getBg());
        destField.setForeground(ThemeManager.getText());
        JButton btnP = ThemeManager.btnSecondary("📂");
        btnP.setPreferredSize(new Dimension(45, 30));
        btnP.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION)
                destField.setText(fc.getSelectedFile().getAbsolutePath());
        });
        destPanel.add(destField, BorderLayout.CENTER);
        destPanel.add(btnP, BorderLayout.EAST);
        body.add(destPanel, gbc);
        main.add(body, BorderLayout.CENTER);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setVisible(false);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(ThemeManager.getCard());
        footer.setBorder(BorderFactory.createEmptyBorder(8, 20, 14, 20));
        JLabel status = new JLabel(" ");
        status.setFont(new Font("Arial", Font.ITALIC, 11));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnAnnuler = ThemeManager.btnSecondary("✖ Annuler");
        AnimationManager.AnimatedButton btnGen = new AnimationManager.AnimatedButton(
            "📄 Générer", new Color(200, 40, 30), new Color(220, 60, 45), new Color(170, 25, 15));
        btnGen.setForeground(Color.WHITE);
        btnGen.setPreferredSize(new Dimension(130, 38));

        btnAnnuler.addActionListener(e -> dialog.dispose());
        btnGen.addActionListener(e -> {
            Cours c = (Cours) combo.getSelectedItem();
            if (c == null) return;
            bar.setVisible(true);
            btnGen.setEnabled(false);
            status.setText("⏳ Génération...");
            status.setForeground(ThemeManager.ACCENT_ORANGE);
            SwingWorker<String, Void> w = new SwingWorker<>() {
                protected String doInBackground() throws Exception {
                    Utilisateur ens = new UtilisateurDAO().findById(c.getEnseignantId());
                    List<Seance> seances = new SeanceDAO().listerParCours(c.getId());
                    FicheSuivi fiche = new FicheSuivi(c, ens, seances);
                    String chemin = destField.getText() + "/fiche_" + c.getIntitule().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
                    return new PdfService().genererFiche(fiche, chemin);
                }
                protected void done() {
                    bar.setVisible(false);
                    btnGen.setEnabled(true);
                    try {
                        String path = get();
                        status.setText("✅ PDF généré !");
                        status.setForeground(ThemeManager.ACCENT_GREEN);
                        int r = JOptionPane.showConfirmDialog(dialog,
                            "<html><b>✅ PDF généré !</b><br><small>" + path + "</small><br>Ouvrir maintenant ?</html>",
                            "Succès", JOptionPane.YES_NO_OPTION);
                        if (r == JOptionPane.YES_OPTION) Desktop.getDesktop().open(new File(path));
                        dialog.dispose();
                    } catch (Exception ex) {
                        status.setText("❌ " + ex.getMessage());
                    }
                }
            };
            w.execute();
        });

        btns.add(btnAnnuler);
        btns.add(btnGen);
        footer.add(bar, BorderLayout.NORTH);
        footer.add(status, BorderLayout.WEST);
        footer.add(btns, BorderLayout.EAST);
        main.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }
}
