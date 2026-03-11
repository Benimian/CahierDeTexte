package com.esitec.cahier.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    public enum Theme { LIGHT, DARK }
    private static Theme currentTheme = Theme.LIGHT;
    private static final List<Runnable> listeners = new ArrayList<>();

    public static final Color LIGHT_BG           = new Color(245, 246, 250);
    public static final Color LIGHT_CARD         = Color.WHITE;
    public static final Color LIGHT_TEXT         = new Color(30, 30, 30);
    public static final Color LIGHT_BORDER       = new Color(220, 220, 220);
    public static final Color LIGHT_TABLE_HEADER = new Color(52, 73, 94);
    public static final Color LIGHT_TABLE_ALT    = new Color(236, 240, 241);

    public static final Color DARK_BG            = new Color(18, 18, 28);
    public static final Color DARK_CARD          = new Color(30, 30, 46);
    public static final Color DARK_TEXT          = new Color(220, 220, 235);
    public static final Color DARK_BORDER        = new Color(60, 60, 80);
    public static final Color DARK_TABLE_HEADER  = new Color(40, 40, 60);
    public static final Color DARK_TABLE_ALT     = new Color(35, 35, 52);

    public static final Color ACCENT_BLUE   = new Color(30, 60, 114);
    public static final Color ACCENT_GREEN  = new Color(39, 174, 96);
    public static final Color ACCENT_RED    = new Color(231, 76, 60);
    public static final Color ACCENT_ORANGE = new Color(243, 156, 18);
    public static final Color ACCENT_PURPLE = new Color(142, 68, 173);
    public static final Color ACCENT_TEAL   = new Color(26, 188, 156);

    public static void toggle() {
        currentTheme = isDark() ? Theme.LIGHT : Theme.DARK;
        notifyListeners();
    }

    public static boolean isDark()           { return currentTheme == Theme.DARK; }
    public static Color getBg()              { return isDark() ? DARK_BG           : LIGHT_BG; }
    public static Color getCard()            { return isDark() ? DARK_CARD         : LIGHT_CARD; }
    public static Color getText()            { return isDark() ? DARK_TEXT         : LIGHT_TEXT; }
    public static Color getBorder()          { return isDark() ? DARK_BORDER       : LIGHT_BORDER; }
    public static Color getTableHeader()     { return isDark() ? DARK_TABLE_HEADER : LIGHT_TABLE_HEADER; }
    public static Color getTableAlt()        { return isDark() ? DARK_TABLE_ALT    : LIGHT_TABLE_ALT; }
    public static String getThemeIcon()      { return isDark() ? "☀️" : "🌙"; }

    public static void addListener(Runnable r) { if (!listeners.contains(r)) listeners.add(r); }
    private static void notifyListeners()      { for (Runnable r : listeners) r.run(); }

    // ════════════════════════════════════════════════════════════════════
    // BOUTON THÈME ANIMÉ — toggle switch style
    // ════════════════════════════════════════════════════════════════════
    public static JButton createThemeButton(Runnable extraCallback) {
        JButton btn = new JButton() {
            private float thumbPos = isDark() ? 1f : 0f; // 0=clair, 1=sombre
            private javax.swing.Timer anim;

            {
                setPreferredSize(new Dimension(70, 30));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setToolTipText(isDark() ? "Passer en mode clair" : "Passer en mode sombre");

                addActionListener(e -> {
                    boolean goingDark = thumbPos < 0.5f;
                    float target = goingDark ? 1f : 0f;
                    if (anim != null) anim.stop();
                    anim = new javax.swing.Timer(12, null);
                    anim.addActionListener(ev -> {
                        float diff = target - thumbPos;
                        thumbPos += diff * 0.15f;
                        if (Math.abs(diff) < 0.01f) {
                            thumbPos = target;
                            anim.stop();
                        }
                        setToolTipText(isDark() ? "Passer en mode clair" : "Passer en mode sombre");
                        repaint();
                    });
                    anim.start();
                    repaint();
                });
            }

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int pad = 3;
                int thumbSize = h - pad * 2;

                // Fond du toggle
                Color bgOff = new Color(200, 210, 230);
                Color bgOn  = new Color(40, 50, 90);
                Color bg    = blend(bgOff, bgOn, thumbPos);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, h, h);

                // Étoiles (mode sombre)
                if (thumbPos > 0.3f) {
                    g2.setColor(new Color(255, 255, 255, (int)(180 * thumbPos)));
                    g2.fillOval(10, 5, 3, 3);
                    g2.fillOval(18, 10, 2, 2);
                    g2.fillOval(14, 16, 2, 2);
                }

                // Soleil/rayons (mode clair)
                if (thumbPos < 0.7f) {
                    float a = 1f - thumbPos;
                    g2.setColor(new Color(255, 220, 80, (int)(200 * a)));
                    int sx = w - pad - thumbSize / 2;
                    int sy = h / 2;
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.toRadians(i * 45);
                        int x1 = (int)(sx + Math.cos(angle) * (thumbSize / 2 - 2));
                        int y1 = (int)(sy + Math.sin(angle) * (thumbSize / 2 - 2));
                        int x2 = (int)(sx + Math.cos(angle) * (thumbSize / 2 + 3));
                        int y2 = (int)(sy + Math.sin(angle) * (thumbSize / 2 + 3));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                }

                // Thumb (cercle glissant)
                int thumbX = pad + (int)(thumbPos * (w - thumbSize - pad * 2));
                Color thumbColor = blend(new Color(255, 220, 80), new Color(200, 210, 255), thumbPos);
                // Ombre
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval(thumbX + 1, pad + 1, thumbSize, thumbSize);
                // Thumb
                g2.setColor(thumbColor);
                g2.fillOval(thumbX, pad, thumbSize, thumbSize);
                // Brillance
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillOval(thumbX + 3, pad + 2, thumbSize / 2, thumbSize / 3);

                g2.dispose();
            }

            private Color blend(Color a, Color b, float t) {
                t = Math.max(0, Math.min(1, t));
                return new Color(
                    (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                    (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                    (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
                );
            }
        };

        // Action réelle (après animation toggle)
        btn.addActionListener(e -> {
            if (extraCallback != null) extraCallback.run();
        });

        return btn;
    }

    // ── Fabrique de boutons ───────────────────────────────────────────
    public static JButton btnPrimary(String t)   { return makeBtn(t, ACCENT_BLUE,   Color.WHITE); }
    public static JButton btnSuccess(String t)   { return makeBtn(t, ACCENT_GREEN,  Color.WHITE); }
    public static JButton btnDanger(String t)    { return makeBtn(t, ACCENT_RED,    Color.WHITE); }
    public static JButton btnWarning(String t)   { return makeBtn(t, ACCENT_ORANGE, Color.WHITE); }
    public static JButton btnSecondary(String t) { return makeBtn(t, new Color(100, 110, 130), Color.WHITE); }
    public static JButton btnPurple(String t)    { return makeBtn(t, ACCENT_PURPLE, Color.WHITE); }
    public static JButton btnTeal(String t)      { return makeBtn(t, ACCENT_TEAL,   Color.WHITE); }

    private static JButton makeBtn(String text, Color bg, Color fg) {
        AnimationManager.AnimatedButton btn = new AnimationManager.AnimatedButton(
            text, bg, bg.brighter(), bg.darker()
        );
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        return btn;
    }

    public static void applyTo(JComponent c) { ThemeTransition.refreshChildren(c); }
}
