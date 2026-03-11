package com.esitec.cahier.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class AnimationManager {

    // ════════════════════════════════════════════════════════════════════
    // 1. FADE IN — apparition progressive d'un composant
    // ════════════════════════════════════════════════════════════════════
    public static void fadeIn(JComponent comp, int durationMs) {
        if (!(comp instanceof FadePanel fp)) return;
        fp.setAlpha(0f);
        comp.setVisible(true);
        final float[] alpha = {0f};
        int steps = 30;
        int delay = durationMs / steps;

        Timer t = new Timer(delay, null);
        t.addActionListener(e -> {
            alpha[0] += 1f / steps;
            if (alpha[0] >= 1f) { alpha[0] = 1f; t.stop(); }
            fp.setAlpha(alpha[0]);
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 2. SLIDE IN — glissement depuis le bas
    // ════════════════════════════════════════════════════════════════════
    public static void slideInFromBottom(JComponent comp, int durationMs) {
        Rectangle target = comp.getBounds();
        int startY = target.y + 40;
        comp.setBounds(target.x, startY, target.width, target.height);
        comp.setVisible(true);

        final int[] currentY = {startY};
        int steps = 20;
        int delay = durationMs / steps;

        Timer t = new Timer(delay, null);
        t.addActionListener(e -> {
            currentY[0] -= 2;
            if (currentY[0] <= target.y) { currentY[0] = target.y; t.stop(); }
            comp.setBounds(target.x, currentY[0], target.width, target.height);
            comp.repaint();
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 3. BOUNCE — effet rebond sur un label/valeur
    // ════════════════════════════════════════════════════════════════════
    public static void bounce(JComponent comp) {
        final float[] scale = {1f};
        final boolean[] growing = {true};
        final int[] ticks = {0};

        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            ticks[0]++;
            if (growing[0]) {
                scale[0] += 0.04f;
                if (scale[0] >= 1.15f) growing[0] = false;
            } else {
                scale[0] -= 0.03f;
                if (scale[0] <= 1f) { scale[0] = 1f; t.stop(); }
            }
            comp.repaint();
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 4. SHAKE — secousse sur un champ invalide
    // ════════════════════════════════════════════════════════════════════
    public static void shake(JComponent comp) {
        final int[] offsets = {0, 8, -8, 6, -6, 4, -4, 2, -2, 0};
        final int[] i = {0};
        Point origin = comp.getLocation();

        Timer t = new Timer(30, null);
        t.addActionListener(e -> {
            if (i[0] >= offsets.length) {
                comp.setLocation(origin);
                t.stop();
                return;
            }
            comp.setLocation(origin.x + offsets[i[0]], origin.y);
            i[0]++;
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 5. PULSE — clignotement doux d'un composant
    // ════════════════════════════════════════════════════════════════════
    public static void pulse(JComponent comp, Color baseColor, Color pulseColor, int times) {
        final int[] count = {0};
        final boolean[] toBase = {false};

        Timer t = new Timer(300, null);
        t.addActionListener(e -> {
            comp.setBackground(toBase[0] ? baseColor : pulseColor);
            toBase[0] = !toBase[0];
            count[0]++;
            if (count[0] >= times * 2) { comp.setBackground(baseColor); t.stop(); }
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 6. COUNT UP — compteur animé (ex: 0 → 42)
    // ════════════════════════════════════════════════════════════════════
    public static void countUp(JLabel label, int targetValue, int durationMs, String suffix) {
        final int[] current = {0};
        int steps = 40;
        int delay = Math.max(1, durationMs / steps);
        int increment = Math.max(1, targetValue / steps);

        Timer t = new Timer(delay, null);
        t.addActionListener(e -> {
            current[0] = Math.min(current[0] + increment, targetValue);
            label.setText(current[0] + suffix);
            if (current[0] >= targetValue) t.stop();
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 7. HOVER SCALE — effet zoom au survol d'un bouton
    // ════════════════════════════════════════════════════════════════════
    public static void addHoverScale(JButton btn, Color normalBg, Color hoverBg) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                animateBgColor(btn, normalBg, hoverBg, 150);
            }
            public void mouseExited(MouseEvent e) {
                animateBgColor(btn, hoverBg, normalBg, 150);
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════
    // 8. COLOR TRANSITION — transition douce de couleur de fond
    // ════════════════════════════════════════════════════════════════════
    public static void animateBgColor(JComponent comp, Color from, Color to, int durationMs) {
        final float[] p = {0f};
        int steps = 15;
        int delay = Math.max(1, durationMs / steps);

        Timer t = new Timer(delay, null);
        t.addActionListener(e -> {
            p[0] = Math.min(p[0] + 1f / steps, 1f);
            int r = (int)(from.getRed()   + (to.getRed()   - from.getRed())   * p[0]);
            int g = (int)(from.getGreen() + (to.getGreen() - from.getGreen()) * p[0]);
            int b = (int)(from.getBlue()  + (to.getBlue()  - from.getBlue())  * p[0]);
            comp.setBackground(new Color(r, g, b));
            if (p[0] >= 1f) t.stop();
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 9. TYPEWRITER — texte qui s'écrit lettre par lettre
    // ════════════════════════════════════════════════════════════════════
    public static void typeWriter(JLabel label, String fullText, int delayPerChar) {
        label.setText("");
        final int[] idx = {0};
        Timer t = new Timer(delayPerChar, null);
        t.addActionListener(e -> {
            if (idx[0] <= fullText.length()) {
                label.setText(fullText.substring(0, idx[0]));
                idx[0]++;
            } else {
                t.stop();
            }
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // 10. PROGRESS BAR ANIMATED — remplissage animé
    // ════════════════════════════════════════════════════════════════════
    public static void animateProgressBar(JProgressBar bar, int targetValue, int durationMs) {
        bar.setValue(0);
        final int[] current = {0};
        int steps = 40;
        int delay = Math.max(1, durationMs / steps);
        int inc   = Math.max(1, targetValue / steps);

        Timer t = new Timer(delay, null);
        t.addActionListener(e -> {
            current[0] = Math.min(current[0] + inc, targetValue);
            bar.setValue(current[0]);
            if (current[0] >= targetValue) t.stop();
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════
    // PANNEAU AVEC TRANSPARENCE (pour fadeIn)
    // ════════════════════════════════════════════════════════════════════
    public static class FadePanel extends JPanel {
        private float alpha = 1f;

        public FadePanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // BOUTON ANIMÉ — hover + press intégrés
    // ════════════════════════════════════════════════════════════════════
    public static class AnimatedButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressBg;
        private Color currentBg;

        public AnimatedButton(String text, Color normalBg, Color hoverBg, Color pressBg) {
            super(text);
            this.normalBg  = normalBg;
            this.hoverBg   = hoverBg;
            this.pressBg   = pressBg;
            this.currentBg = normalBg;

            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 12));
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { animateBgColor(AnimatedButton.this, currentBg, hoverBg, 150); currentBg = hoverBg; }
                public void mouseExited(MouseEvent e)  { animateBgColor(AnimatedButton.this, currentBg, normalBg, 150); currentBg = normalBg; }
                public void mousePressed(MouseEvent e) { setBackground(pressBg); currentBg = pressBg; }
                public void mouseReleased(MouseEvent e){ animateBgColor(AnimatedButton.this, currentBg, hoverBg, 100); currentBg = hoverBg; }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getBackground() != null ? getBackground() : normalBg;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
