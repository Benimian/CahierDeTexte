package com.esitec.cahier.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class AvatarPanel extends JPanel {

    private String initiales;
    private Color  bgColor;
    private final int size;

    // Palette de couleurs pour les avatars
    private static final Color[] PALETTE = {
        new Color(41,  128, 185),  // bleu
        new Color(39,  174,  96),  // vert
        new Color(142,  68, 173),  // violet
        new Color(231,  76,  60),  // rouge
        new Color(230, 126,  34),  // orange
        new Color(26,  188, 156),  // turquoise
        new Color(52,  152, 219),  // bleu clair
        new Color(155,  89, 182),  // violet clair
    };

    public AvatarPanel(String nomComplet, int size) {
        this.size = size;
        setOpaque(false);
        setPreferredSize(new Dimension(size, size));
        update(nomComplet);
    }

    public void update(String nomComplet) {
        this.initiales = extraireInitiales(nomComplet);
        // Couleur déterministe basée sur le nom
        int idx = Math.abs(nomComplet.hashCode()) % PALETTE.length;
        this.bgColor = PALETTE[idx];
        repaint();
    }

    private String extraireInitiales(String nomComplet) {
        if (nomComplet == null || nomComplet.isBlank()) return "?";
        String[] parts = nomComplet.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length-1].charAt(0)).toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Ombre portée
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fill(new Ellipse2D.Float(3, 3, size - 2, size - 2));

        // Cercle de fond avec dégradé
        GradientPaint gp = new GradientPaint(
            0, 0, bgColor.brighter(),
            size, size, bgColor.darker());
        g2.setPaint(gp);
        g2.fill(new Ellipse2D.Float(0, 0, size - 2, size - 2));

        // Bordure blanche
        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Float(1, 1, size - 4, size - 4));

        // Initiales
        g2.setColor(Color.WHITE);
        int fontSize = size / 3;
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - 2 - fm.stringWidth(initiales)) / 2;
        int y = (size - 2 - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initiales, x, y);
        g2.dispose();
    }
}
