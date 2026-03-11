package com.esitec.cahier.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.esitec.cahier.ui.utils.AnimationManager;

public class SplashScreen extends JWindow {

    private JProgressBar progressBar;
    private JLabel lblStatus;
    private JLabel lblLogo;
    private float alpha = 0f;

    public SplashScreen() {
        setSize(500, 300);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 500, 300, 30, 30));
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fond dégradé
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(15, 30, 75),
                    getWidth(), getHeight(), new Color(50, 15, 90)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Cercles décoratifs
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-40, -40, 200, 200);
                g2.fillOval(getWidth() - 100, getHeight() - 80, 180, 180);
                g2.dispose();
            }
        };
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // ── Centre ────────────────────────────────────────────────────
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        lblLogo = new JLabel("📚", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Arial", Font.PLAIN, 52));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNom = new JLabel("ESITEC", SwingConstants.CENTER);
        lblNom.setFont(new Font("Arial", Font.BOLD, 32));
        lblNom.setForeground(Color.WHITE);
        lblNom.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSous = new JLabel("", SwingConstants.CENTER);
        lblSous.setFont(new Font("Arial", Font.ITALIC, 13));
        lblSous.setForeground(new Color(160, 180, 230));
        lblSous.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(lblLogo);
        center.add(Box.createVerticalStrut(6));
        center.add(lblNom);
        center.add(Box.createVerticalStrut(4));
        center.add(lblSous);
        main.add(center, BorderLayout.CENTER);

        // ── Bas ───────────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);

        lblStatus = new JLabel("Initialisation...", SwingConstants.CENTER);
        lblStatus.setForeground(new Color(160, 180, 230));
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 11));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 6));
        progressBar.setBackground(new Color(50, 50, 90));
        progressBar.setForeground(new Color(100, 160, 255));
        progressBar.setBorderPainted(false);

        bottom.add(lblStatus, BorderLayout.NORTH);
        bottom.add(progressBar, BorderLayout.SOUTH);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    public void demarrer(Runnable onComplete) {
        setVisible(true);

        // Animation fade-in de la fenêtre
        final float[] winAlpha = {0f};
        Timer fadeIn = new Timer(20, null);
        fadeIn.addActionListener(e -> {
            winAlpha[0] += 0.06f;
            if (winAlpha[0] >= 1f) { winAlpha[0] = 1f; fadeIn.stop(); }
            setOpacity(winAlpha[0]);
        });
        fadeIn.start();

        // Animation logo bounce
        Timer logoBounce = new Timer(80, null);
        final float[] scale = {0.5f};
        final boolean[] growing = {true};
        logoBounce.addActionListener(e -> {
            if (growing[0]) {
                scale[0] += 0.08f;
                if (scale[0] >= 1.1f) growing[0] = false;
            } else {
                scale[0] -= 0.04f;
                if (scale[0] <= 1f) { scale[0] = 1f; logoBounce.stop(); }
            }
            lblLogo.setFont(new Font("Arial", Font.PLAIN, (int)(52 * scale[0])));
        });
        logoBounce.start();

        // Étapes chargement
        String[] steps = {
            "Connexion à la base de données...",
            "Chargement des utilisateurs...",
            "Chargement des cours...",
            "Préparation de l'interface...",
            "Démarrage de l'application..."
        };

        // Progress animée
        AnimationManager.animateProgressBar(progressBar, 100, 2500);

        Timer stepTimer = new Timer(500, null);
        final int[] step = {0};
        stepTimer.addActionListener(e -> {
            if (step[0] < steps.length) {
                AnimationManager.typeWriter(lblStatus, steps[step[0]], 25);
                step[0]++;
            } else {
                stepTimer.stop();
                // Fade-out et lancement
                Timer fadeOut = new Timer(20, null);
                fadeOut.addActionListener(ev -> {
                    winAlpha[0] -= 0.06f;
                    if (winAlpha[0] <= 0f) {
                        winAlpha[0] = 0f;
                        fadeOut.stop();
                        dispose();
                        SwingUtilities.invokeLater(onComplete);
                    }
                    setOpacity(Math.max(0f, winAlpha[0]));
                });
                fadeOut.start();
            }
        });
        stepTimer.start();
    }
}
