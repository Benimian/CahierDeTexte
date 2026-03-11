package com.esitec.cahier.ui.utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ThemeTransition {

    private static final List<JFrame> registeredFrames   = new ArrayList<>();
    private static final List<Runnable> refreshCallbacks = new ArrayList<>();

    public static void register(JFrame frame, Runnable cb) {
        if (!registeredFrames.contains(frame)) {
            registeredFrames.add(frame);
            refreshCallbacks.add(cb);
        }
    }

    public static void unregister(JFrame frame) {
        int idx = registeredFrames.indexOf(frame);
        if (idx >= 0) { registeredFrames.remove(idx); refreshCallbacks.remove(idx); }
    }

    // ── Bascule le thème avec effet ripple ────────────────────────────
    public static void toggleWithEffect(JFrame source) {
        BufferedImage before = captureFrame(source);
        ThemeManager.toggle();

        // Rafraîchit toutes les fenêtres enregistrées
        for (int i = 0; i < registeredFrames.size(); i++) {
            JFrame frame = registeredFrames.get(i);
            applyThemeToFrame(frame);
            if (refreshCallbacks.get(i) != null) refreshCallbacks.get(i).run();
        }

        if (before != null) playRipple(source, before);
    }

    // ── Applique le thème complet sur une fenêtre ─────────────────────
    public static void applyThemeToFrame(JFrame frame) {
        frame.getContentPane().setBackground(ThemeManager.getBg());
        deepRefresh(frame.getRootPane());
        frame.repaint();
        frame.revalidate();
    }

    // ── Parcourt TOUS les composants récursivement ────────────────────
    public static void deepRefresh(Component comp) {
        if (comp == null) return;

        // ── Labels ────────────────────────────────────────────────────
        if (comp instanceof JLabel lbl) {
            // Ne pas toucher les labels dans les headers colorés (fond non getBg)
            Color parentBg = comp.getParent() != null ? comp.getParent().getBackground() : null;
            if (parentBg == null || isNeutralBg(parentBg)) {
                lbl.setForeground(ThemeManager.getText());
            }
        }

        // ── Boutons — ne pas écraser leurs couleurs custom ────────────
        // (les AnimatedButton gèrent eux-mêmes)

        // ── Champs texte ──────────────────────────────────────────────
        if (comp instanceof JTextField tf && !(comp instanceof JPasswordField)) {
            tf.setBackground(ThemeManager.getBg());
            tf.setForeground(ThemeManager.getText());
            tf.setCaretColor(ThemeManager.getText());
        }
        if (comp instanceof JPasswordField pf) {
            pf.setBackground(ThemeManager.getBg());
            pf.setForeground(ThemeManager.getText());
            pf.setCaretColor(ThemeManager.getText());
        }

        // ── TextArea ──────────────────────────────────────────────────
        if (comp instanceof JTextArea ta) {
            ta.setBackground(ThemeManager.getBg());
            ta.setForeground(ThemeManager.getText());
            ta.setCaretColor(ThemeManager.getText());
        }

        // ── ComboBox ──────────────────────────────────────────────────
        if (comp instanceof JComboBox<?> cb) {
            cb.setBackground(ThemeManager.getBg());
            cb.setForeground(ThemeManager.getText());
        }

        // ── Table ─────────────────────────────────────────────────────
        if (comp instanceof JTable table) {
            table.setBackground(ThemeManager.getCard());
            table.setForeground(ThemeManager.getText());
            table.setGridColor(ThemeManager.getBorder());
            table.setSelectionBackground(new Color(30, 60, 114, 80));
            table.setSelectionForeground(ThemeManager.getText());
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                header.setBackground(ThemeManager.getTableHeader());
                header.setForeground(Color.WHITE);
            }
            // Renderer par défaut
            table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    Component cell = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    if (!sel) {
                        cell.setBackground(r % 2 == 0 ? ThemeManager.getCard() : ThemeManager.getTableAlt());
                        cell.setForeground(ThemeManager.getText());
                    }
                    ((JLabel) cell).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                    return cell;
                }
            });
            table.repaint();
        }

        // ── ScrollPane ────────────────────────────────────────────────
        if (comp instanceof JScrollPane sp) {
            sp.setBackground(ThemeManager.getBg());
            sp.getViewport().setBackground(ThemeManager.getBg());
            sp.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder()));
        }

        // ── TabbedPane ────────────────────────────────────────────────
        if (comp instanceof JTabbedPane tp) {
            tp.setBackground(ThemeManager.getBg());
            tp.setForeground(ThemeManager.getText());
        }

        // ── Panel générique ───────────────────────────────────────────
        if (comp instanceof JPanel panel) {
            Color bg = panel.getBackground();
            // Ne touche pas les panels avec couleur d'accent (headers)
            if (bg != null && isNeutralBg(bg)) {
                panel.setBackground(ThemeManager.getBg());
                panel.setForeground(ThemeManager.getText());
            }
            // Recolore le TitledBorder si présent
            if (panel.getBorder() instanceof TitledBorder tb) {
                tb.setTitleColor(ThemeManager.getText());
            }
        }

        // ── Viewport ─────────────────────────────────────────────────
        if (comp instanceof JViewport vp) {
            vp.setBackground(ThemeManager.getBg());
        }

        // ── Récursion sur enfants ─────────────────────────────────────
        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                deepRefresh(child);
            }
        }

        if (comp instanceof JComponent jc) jc.repaint();
    }

    // ── Détermine si un fond est "neutre" (à recolorer) ───────────────
    private static boolean isNeutralBg(Color c) {
        if (c == null) return true;
        // Couleurs d'accent à NE PAS toucher
        Color[] accents = {
            new Color(20, 40, 80),   // chef header
            new Color(30, 110, 70),  // enseignant header
            new Color(100, 40, 140), // responsable header
            new Color(30, 60, 114),  // accent blue
            new Color(200, 40, 30),  // accent red
            new Color(39, 174, 96),  // accent green
            new Color(15, 30, 75),   // title bar
            new Color(20, 40, 80),   // stat header
        };
        for (Color accent : accents) {
            if (colorClose(c, accent, 30)) return false;
        }
        return true;
    }

    private static boolean colorClose(Color a, Color b, int tolerance) {
        return Math.abs(a.getRed()   - b.getRed())   < tolerance &&
               Math.abs(a.getGreen() - b.getGreen()) < tolerance &&
               Math.abs(a.getBlue()  - b.getBlue())  < tolerance;
    }

    // ── Rafraîchit uniquement (sans toggle) ───────────────────────────
    public static void refreshChildren(JComponent root) {
        deepRefresh(root);
    }

    // ── Capture écran ─────────────────────────────────────────────────
    private static BufferedImage captureFrame(JFrame frame) {
        try {
            BufferedImage img = new BufferedImage(
                frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
            frame.paint(img.getGraphics());
            return img;
        } catch (Exception e) { return null; }
    }

    // ── Effet ripple ──────────────────────────────────────────────────
    private static void playRipple(JFrame frame, BufferedImage before) {
        JPanel overlay = new JPanel() {
            float progress = 0f;
            int maxR = (int) Math.sqrt(Math.pow(frame.getWidth(), 2) + Math.pow(frame.getHeight(), 2)) + 50;
            Color fill = ThemeManager.isDark() ? new Color(18, 18, 28) : new Color(245, 246, 250);

            {
                setOpaque(false);
                javax.swing.Timer t = new javax.swing.Timer(12, null);
                t.addActionListener(e -> {
                    progress += 0.045f;
                    if (progress >= 1f) {
                        t.stop();
                        frame.getLayeredPane().remove(this);
                        frame.repaint();
                    }
                    repaint();
                });
                t.start();
            }

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(before, 0, 0, null);

                float eased = easeInOut(progress);
                int r = (int)(eased * maxR);
                int cx = getWidth() / 2, cy = getHeight() / 2;

                g2.setComposite(AlphaComposite.Clear);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.dispose();
            }

            float easeInOut(float t) { return t < 0.5f ? 2*t*t : -1+(4-2*t)*t; }
        };

        overlay.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        frame.getLayeredPane().add(overlay, JLayeredPane.DRAG_LAYER);
        frame.repaint();
    }
}
