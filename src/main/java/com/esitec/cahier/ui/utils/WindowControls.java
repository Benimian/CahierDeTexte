package com.esitec.cahier.ui.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class WindowControls extends JPanel {

    private Point dragOrigin;

    public WindowControls(JFrame frame, String titre, Color accentColor) {
        setLayout(new BorderLayout());
        setBackground(accentColor.darker());
        setPreferredSize(new Dimension(0, 36));

        // ── Titre de la fenêtre ───────────────────────────────────────
        JLabel lblTitre = new JLabel("By BENIMIANZ " + titre);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitre.setForeground(new Color(200, 215, 240));
        add(lblTitre, BorderLayout.WEST);

        // ── Boutons contrôle ──────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 4));
        btnPanel.setOpaque(false);

        JButton btnMin   = makeControlBtn("—", new Color(243, 156, 18), new Color(220, 130, 10));
        JButton btnMax   = makeControlBtn("+", new Color(39, 174, 96),  new Color(28, 140, 75));
        JButton btnClose = makeControlBtn("✕", new Color(231, 76, 60),   new Color(200, 50, 40));

        btnMin.setToolTipText("Réduire");
        btnMax.setToolTipText("Agrandir / Restaurer");
        btnClose.setToolTipText("Fermer");

        btnMin.addActionListener(e -> frame.setState(Frame.ICONIFIED));
        btnMax.addActionListener(e -> {
            if (frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                frame.setExtendedState(Frame.NORMAL);
                btnMax.setText("⬜");
            } else {
                frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                btnMax.setText("❐");
            }
        });
        btnClose.addActionListener(e -> {
            // Fade out avant fermeture
            Timer t = new Timer(12, null);
            final float[] a = {1f};
            t.addActionListener(ev -> {
                a[0] -= 0.08f;
                if (a[0] <= 0f) { t.stop(); frame.dispose(); }
                try { frame.setOpacity(Math.max(0f, a[0])); } catch (Exception ignored) {}
            });
            t.start();
        });

        btnPanel.add(btnMin);
        btnPanel.add(btnMax);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.EAST);

        // ── Drag de la fenêtre ────────────────────────────────────────
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (frame.getExtendedState() != Frame.MAXIMIZED_BOTH)
                    dragOrigin = e.getPoint();
            }
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) btnMax.doClick();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragOrigin != null && frame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
                    Point loc = frame.getLocation();
                    frame.setLocation(loc.x + e.getX() - dragOrigin.x,
                                      loc.y + e.getY() - dragOrigin.y);
                }
            }
        });
    }

    private JButton makeControlBtn(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,
                    (getWidth() - fm.stringWidth(text)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
