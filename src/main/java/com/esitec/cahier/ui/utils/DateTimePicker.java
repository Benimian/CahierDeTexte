package com.esitec.cahier.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateTimePicker extends JPanel {

    // ================================================================
    // DATE PICKER
    // ================================================================
    public static class DatePicker extends JPanel {
        private LocalDate  selected;
        private YearMonth  viewing;
        private JLabel     lblMonth;
        private JPanel     calGrid;
        private JTextField displayField;
        private JPopupMenu popup;
        private final List<Runnable> listeners = new ArrayList<>();
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final String[] JOURS = {"Lu","Ma","Me","Je","Ve","Sa","Di"};
        private static final String[] MOIS  = {
            "Janvier","Fevrier","Mars","Avril","Mai","Juin",
            "Juillet","Aout","Septembre","Octobre","Novembre","Decembre"};

        public DatePicker() { this(LocalDate.now()); }
        public DatePicker(LocalDate initial) {
            this.selected = initial;
            this.viewing  = YearMonth.of(initial.getYear(), initial.getMonth());
            setLayout(new BorderLayout());
            setOpaque(false);
            buildUI();
        }

        private void buildUI() {
            displayField = new JTextField(selected.format(FMT));
            displayField.setEditable(false);
            displayField.setBackground(ThemeManager.getBg());
            displayField.setForeground(ThemeManager.getText());
            displayField.setFont(new Font("Arial", Font.BOLD, 13));
            displayField.setCursor(new Cursor(Cursor.HAND_CURSOR));
            displayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.ACCENT_BLUE, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

            JButton btnCal = new JButton("Choisir") {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? ThemeManager.ACCENT_BLUE.brighter() : ThemeManager.ACCENT_BLUE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnCal.setForeground(Color.WHITE);
            btnCal.setFont(new Font("Arial", Font.BOLD, 11));
            btnCal.setBorderPainted(false);
            btnCal.setFocusPainted(false);
            btnCal.setContentAreaFilled(false);
            btnCal.setPreferredSize(new Dimension(72, 32));
            btnCal.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setOpaque(false);
            row.add(displayField, BorderLayout.CENTER);
            row.add(btnCal,       BorderLayout.EAST);
            add(row, BorderLayout.CENTER);

            popup = new JPopupMenu();
            popup.setBorder(BorderFactory.createLineBorder(ThemeManager.ACCENT_BLUE, 1));

            ActionListener open = e -> {
                popup.removeAll();
                popup.add(buildCalendar());
                popup.show(displayField, 0, displayField.getHeight());
            };
            btnCal.addActionListener(open);
            displayField.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { open.actionPerformed(null); }
            });
        }

        private JPanel buildCalendar() {
            JPanel cal = new JPanel(new BorderLayout(0, 4));
            cal.setBackground(ThemeManager.getCard());
            cal.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            cal.setPreferredSize(new Dimension(240, 220));

            JPanel nav = new JPanel(new BorderLayout());
            nav.setBackground(ThemeManager.ACCENT_BLUE);
            nav.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            JButton prev = navBtn("<");
            JButton next = navBtn(">");
            lblMonth = new JLabel(MOIS[viewing.getMonthValue()-1] + " " + viewing.getYear(), SwingConstants.CENTER);
            lblMonth.setForeground(Color.WHITE);
            lblMonth.setFont(new Font("Arial", Font.BOLD, 12));
            prev.addActionListener(e -> { viewing = viewing.minusMonths(1); refreshCal(cal); });
            next.addActionListener(e -> { viewing = viewing.plusMonths(1);  refreshCal(cal); });
            nav.add(prev, BorderLayout.WEST);
            nav.add(lblMonth, BorderLayout.CENTER);
            nav.add(next, BorderLayout.EAST);
            cal.add(nav, BorderLayout.NORTH);

            calGrid = new JPanel(new GridLayout(7, 7, 2, 2));
            calGrid.setBackground(ThemeManager.getCard());
            fillGrid();
            cal.add(calGrid, BorderLayout.CENTER);
            return cal;
        }

        private void refreshCal(JPanel cal) {
            lblMonth.setText(MOIS[viewing.getMonthValue()-1] + " " + viewing.getYear());
            calGrid.removeAll(); fillGrid();
            calGrid.revalidate(); calGrid.repaint();
        }

        private void fillGrid() {
            for (String j : JOURS) {
                JLabel l = new JLabel(j, SwingConstants.CENTER);
                l.setFont(new Font("Arial", Font.BOLD, 10));
                l.setForeground(new Color(30, 60, 114));
                calGrid.add(l);
            }
            LocalDate first = viewing.atDay(1);
            int offset = first.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++) calGrid.add(new JLabel(""));
            for (int d = 1; d <= viewing.lengthOfMonth(); d++) {
                LocalDate date = viewing.atDay(d);
                JButton btn = new JButton(String.valueOf(d));
                btn.setFont(new Font("Arial", Font.PLAIN, 11));
                btn.setBorderPainted(false); btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.setMargin(new Insets(1,1,1,1));
                boolean isSel   = date.equals(selected);
                boolean isToday = date.equals(LocalDate.now());
                if (isSel)        { btn.setBackground(ThemeManager.ACCENT_BLUE);   btn.setForeground(Color.WHITE); btn.setOpaque(true); }
                else if (isToday) { btn.setBackground(new Color(230,240,255));      btn.setForeground(ThemeManager.ACCENT_BLUE); btn.setOpaque(true); }
                else              { btn.setBackground(ThemeManager.getCard());      btn.setForeground(ThemeManager.getText()); btn.setOpaque(true); }
                btn.addActionListener(e -> {
                    selected = date;
                    displayField.setText(selected.format(FMT));
                    popup.setVisible(false);
                    listeners.forEach(Runnable::run);
                });
                calGrid.add(btn);
            }
        }

        private JButton navBtn(String t) {
            JButton b = new JButton(t);
            b.setForeground(Color.WHITE); b.setFont(new Font("Arial", Font.BOLD, 11));
            b.setBorderPainted(false); b.setFocusPainted(false); b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        public String getValue() { return selected.format(FMT); }
        public void addChangeListener(Runnable r) { listeners.add(r); }
    }

    // ================================================================
    // TIME RANGE PICKER : debut + fin + duree avec 3 boutons
    // ================================================================
    public static class TimeRangePicker extends JPanel {

        private int startH = 8,  startM = 0;
        private int endH   = 10, endM   = 0;
        private int dureeH = 2,  dureeM = 0;
        private boolean updating = false;

        private JLabel lblStart, lblEnd, lblDuree;
        private final List<Runnable> listeners = new ArrayList<>();

        public TimeRangePicker() {
            setLayout(new GridBagLayout());
            setOpaque(false);
            buildUI();
        }

        private void buildUI() {
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 8, 6, 8);
            g.fill   = GridBagConstraints.HORIZONTAL;

            // ── Ligne 1 : Heure de debut ──────────────────────────
            g.gridx = 0; g.gridy = 0; g.weightx = 0;
            JLabel titreDebut = sectionLabel("Heure de debut :", ThemeManager.ACCENT_GREEN);
            add(titreDebut, g);

            g.gridx = 1; g.weightx = 0;
            lblStart = timeDisplay(startH, startM, ThemeManager.ACCENT_GREEN);
            add(lblStart, g);

            g.gridx = 2; g.weightx = 0;
            JButton btnDebut = actionBtn("Definir le debut", ThemeManager.ACCENT_GREEN);
            btnDebut.addActionListener(e -> ouvrirDialogHeure(true));
            add(btnDebut, g);

            // ── Ligne 2 : Heure de fin ────────────────────────────
            g.gridx = 0; g.gridy = 1; g.weightx = 0;
            add(sectionLabel("Heure de fin :", ThemeManager.ACCENT_RED), g);

            g.gridx = 1; g.weightx = 0;
            lblEnd = timeDisplay(endH, endM, ThemeManager.ACCENT_RED);
            add(lblEnd, g);

            g.gridx = 2; g.weightx = 0;
            JButton btnFin = actionBtn("Definir la fin", ThemeManager.ACCENT_RED);
            btnFin.addActionListener(e -> ouvrirDialogHeure(false));
            add(btnFin, g);

            // ── Separateur ────────────────────────────────────────
            g.gridx = 0; g.gridy = 2; g.gridwidth = 3; g.weightx = 1;
            JSeparator sep = new JSeparator();
            sep.setForeground(ThemeManager.getBorder());
            add(sep, g);
            g.gridwidth = 1;

            // ── Ligne 3 : Duree ───────────────────────────────────
            g.gridx = 0; g.gridy = 3; g.weightx = 0;
            add(sectionLabel("Duree de la seance :", ThemeManager.ACCENT_ORANGE), g);

            g.gridx = 1; g.weightx = 0;
            lblDuree = dureeDisplay(dureeH, dureeM);
            add(lblDuree, g);

            g.gridx = 2; g.weightx = 0;
            JButton btnDuree = actionBtn("Definir la duree", ThemeManager.ACCENT_ORANGE);
            btnDuree.addActionListener(e -> ouvrirDialogDuree());
            add(btnDuree, g);
        }

        // ── Dialog heure (debut ou fin) ───────────────────────────
        private void ouvrirDialogHeure(boolean isStart) {
            int[] h = {isStart ? startH : endH};
            int[] m = {isStart ? startM : endM};
            Color accent = isStart ? ThemeManager.ACCENT_GREEN : ThemeManager.ACCENT_RED;
            String titre  = isStart ? "Definir l'heure de debut" : "Definir l'heure de fin";

            JDialog dlg = buildDialog(titre, accent, 240, 260);

            JPanel body = buildWheelPanel(h, m, accent);
            dlg.getContentPane().add(body, BorderLayout.CENTER);

            JPanel footer = buildFooter(dlg,
                accent,
                "Confirmer",
                e -> {
                    if (isStart) { startH = h[0]; startM = m[0]; lblStart.setText(formatTime(startH, startM)); }
                    else         { endH   = h[0]; endM   = m[0]; lblEnd.setText(formatTime(endH, endM)); }
                    recalcDureeFromTimes();
                    dlg.dispose();
                    listeners.forEach(Runnable::run);
                });
            dlg.getContentPane().add(footer, BorderLayout.SOUTH);
            dlg.setVisible(true);
        }

        // ── Dialog duree ──────────────────────────────────────────
        private void ouvrirDialogDuree() {
            int[] dh = {dureeH};
            int[] dm = {dureeM};
            Color accent = ThemeManager.ACCENT_ORANGE;

            JDialog dlg = buildDialog("Definir la duree de la seance", accent, 280, 320);

            JPanel body = new JPanel(new BorderLayout(0, 10));
            body.setBackground(ThemeManager.getCard());
            body.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

            // Roues
            body.add(buildWheelPanel(dh, dm, accent), BorderLayout.CENTER);

            // Raccourcis rapides
            JPanel shortcuts = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
            shortcuts.setOpaque(false);
            JLabel lblRaccourcis = new JLabel("Raccourcis :");
            lblRaccourcis.setFont(new Font("Arial", Font.BOLD, 10));
            lblRaccourcis.setForeground(ThemeManager.getText());
            shortcuts.add(lblRaccourcis);

            int[][] presets = {{0,30},{1,0},{1,30},{2,0},{3,0},{4,0}};
            String[] names  = {"30 min","1 h","1h30","2 h","3 h","4 h"};
            for (int i = 0; i < presets.length; i++) {
                final int fh = presets[i][0], fm = presets[i][1];
                final String fn = names[i];
                JButton b = new JButton(fn) {
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getModel().isRollover()
                            ? ThemeManager.ACCENT_ORANGE.brighter()
                            : new Color(230, 126, 34, 200));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                b.setForeground(Color.WHITE);
                b.setFont(new Font("Arial", Font.BOLD, 10));
                b.setBorderPainted(false); b.setFocusPainted(false); b.setContentAreaFilled(false);
                b.setPreferredSize(new Dimension(52, 26));
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                b.addActionListener(ev -> {
                    dh[0] = fh; dm[0] = fm;
                    // Reconstruire le dialog avec les nouvelles valeurs
                    dlg.dispose();
                    dureeH = fh; dureeM = fm;
                    lblDuree.setText(formatDuree(dureeH, dureeM));
                    recalcEndFromDuree();
                    listeners.forEach(Runnable::run);
                });
                shortcuts.add(b);
            }
            body.add(shortcuts, BorderLayout.SOUTH);
            dlg.getContentPane().add(body, BorderLayout.CENTER);

            JPanel footer = buildFooter(dlg, accent, "Appliquer",
                e -> {
                    dureeH = dh[0]; dureeM = dm[0];
                    lblDuree.setText(formatDuree(dureeH, dureeM));
                    recalcEndFromDuree();
                    dlg.dispose();
                    listeners.forEach(Runnable::run);
                });
            dlg.getContentPane().add(footer, BorderLayout.SOUTH);
            dlg.setVisible(true);
        }

        // ── Construit un dialog modal ─────────────────────────────
        private JDialog buildDialog(String titre, Color accent, int w, int h) {
            JDialog dlg = new JDialog();
            dlg.setTitle(titre);
            dlg.setModal(true);
            dlg.setUndecorated(true);
            dlg.setSize(w, h);
            dlg.setLocationRelativeTo(this);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(ThemeManager.getCard());
            root.setBorder(BorderFactory.createLineBorder(accent, 2));

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(accent);
            header.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 10));
            JLabel lTitre = new JLabel(titre);
            lTitre.setFont(new Font("Arial", Font.BOLD, 13));
            lTitre.setForeground(Color.WHITE);

            JButton btnX = new JButton("X");
            btnX.setForeground(Color.WHITE);
            btnX.setFont(new Font("Arial", Font.BOLD, 11));
            btnX.setBorderPainted(false); btnX.setFocusPainted(false); btnX.setContentAreaFilled(false);
            btnX.setPreferredSize(new Dimension(26, 22));
            btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnX.addActionListener(e -> dlg.dispose());

            header.add(lTitre, BorderLayout.CENTER);
            header.add(btnX,   BorderLayout.EAST);
            root.add(header, BorderLayout.NORTH);
            dlg.setContentPane(root);
            return dlg;
        }

        // ── Footer avec bouton Confirmer + Annuler ────────────────
        private JPanel buildFooter(JDialog dlg, Color accent, String okLabel,
                                   ActionListener onOk) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            footer.setBackground(ThemeManager.getCard());

            JButton btnAnn = new JButton("Annuler") {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(140, 140, 155));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            btnAnn.setForeground(Color.WHITE);
            btnAnn.setFont(new Font("Arial", Font.BOLD, 12));
            btnAnn.setBorderPainted(false); btnAnn.setFocusPainted(false); btnAnn.setContentAreaFilled(false);
            btnAnn.setPreferredSize(new Dimension(100, 36));
            btnAnn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAnn.addActionListener(e -> dlg.dispose());

            JButton btnOk = new JButton(okLabel) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? accent.brighter() : accent);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            btnOk.setForeground(Color.WHITE);
            btnOk.setFont(new Font("Arial", Font.BOLD, 12));
            btnOk.setBorderPainted(false); btnOk.setFocusPainted(false); btnOk.setContentAreaFilled(false);
            btnOk.setPreferredSize(new Dimension(130, 36));
            btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOk.addActionListener(onOk);

            footer.add(btnAnn);
            footer.add(btnOk);
            return footer;
        }

        // ── Panneau roues HH:MM ───────────────────────────────────
        private JPanel buildWheelPanel(int[] h, int[] m, Color accent) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(ThemeManager.getCard());
            panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JLabel lblH = wheelLabel(String.format("%02d", h[0]), accent);
            JLabel sep  = new JLabel(":", SwingConstants.CENTER);
            sep.setFont(new Font("Arial", Font.BOLD, 30));
            sep.setForeground(ThemeManager.getText());
            JLabel lblM = wheelLabel(String.format("%02d", m[0]), accent);

            JButton hPlus  = wheelBtn("+", accent); JButton hMoins = wheelBtn("-", accent);
            JButton mPlus  = wheelBtn("+", accent); JButton mMoins = wheelBtn("-", accent);

            hPlus.addActionListener(e  -> { h[0]=(h[0]+1)%24;    lblH.setText(String.format("%02d",h[0])); bounce(lblH); });
            hMoins.addActionListener(e -> { h[0]=(h[0]+23)%24;   lblH.setText(String.format("%02d",h[0])); bounce(lblH); });
            mPlus.addActionListener(e  -> { m[0]=(m[0]+5)%60;    lblM.setText(String.format("%02d",m[0])); bounce(lblM); });
            mMoins.addActionListener(e -> { m[0]=(m[0]+55)%60;   lblM.setText(String.format("%02d",m[0])); bounce(lblM); });

            lblH.addMouseWheelListener(we -> { h[0]=(h[0]-we.getWheelRotation()+24)%24;    lblH.setText(String.format("%02d",h[0])); bounce(lblH); });
            lblM.addMouseWheelListener(we -> { m[0]=(m[0]-we.getWheelRotation()*5+60)%60;  lblM.setText(String.format("%02d",m[0])); bounce(lblM); });

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(3, 6, 3, 6);

            // Ligne +
            g.gridx=0; g.gridy=0; panel.add(hPlus,  g);
            g.gridx=2; g.gridy=0; panel.add(mPlus,  g);
            // Ligne chiffres
            g.gridx=0; g.gridy=1; panel.add(lblH,   g);
            g.gridx=1; g.gridy=1; panel.add(sep,    g);
            g.gridx=2; g.gridy=1; panel.add(lblM,   g);
            // Ligne -
            g.gridx=0; g.gridy=2; panel.add(hMoins, g);
            g.gridx=2; g.gridy=2; panel.add(mMoins, g);
            // Hints
            JLabel hH = hint("heures"); JLabel hM = hint("minutes");
            g.gridx=0; g.gridy=3; panel.add(hH, g);
            g.gridx=2; g.gridy=3; panel.add(hM, g);

            return panel;
        }

        // ── Synchronisation ───────────────────────────────────────
        private void recalcDureeFromTimes() {
            if (updating) return; updating = true;
            int diff = (endH*60+endM) - (startH*60+startM);
            if (diff > 0) { dureeH=diff/60; dureeM=diff%60; lblDuree.setText(formatDuree(dureeH,dureeM)); lblDuree.setForeground(ThemeManager.ACCENT_ORANGE); }
            else          { lblDuree.setText("Invalide"); lblDuree.setForeground(ThemeManager.ACCENT_RED); }
            updating = false;
        }

        private void recalcEndFromDuree() {
            if (updating) return; updating = true;
            int total = startH*60+startM+dureeH*60+dureeM;
            endH=(total/60)%24; endM=total%60;
            lblEnd.setText(formatTime(endH, endM));
            updating = false;
        }

        // ── Helpers formatage ─────────────────────────────────────
        private String formatTime(int h, int m)  { return String.format("%02d:%02d", h, m); }
        private String formatDuree(int h, int m) {
            if (h==0&&m==0) return "0 min";
            if (h==0)       return m+" min";
            if (m==0)       return h+"h";
            return h+"h "+m+"min";
        }

        // ── Helpers composants ────────────────────────────────────
        private JLabel sectionLabel(String t, Color c) {
            JLabel l = new JLabel(t);
            l.setFont(new Font("Arial", Font.BOLD, 12));
            l.setForeground(c);
            return l;
        }

        private JLabel timeDisplay(int h, int m, Color accent) {
            JLabel lbl = new JLabel(formatTime(h, m), SwingConstants.CENTER) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ThemeManager.getBg());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            lbl.setFont(new Font("Arial", Font.BOLD, 20));
            lbl.setForeground(accent);
            lbl.setOpaque(false);
            lbl.setPreferredSize(new Dimension(80, 38));
            return lbl;
        }

        private JLabel dureeDisplay(int h, int m) {
            JLabel lbl = new JLabel(formatDuree(h, m), SwingConstants.CENTER) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ThemeManager.getBg());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(ThemeManager.ACCENT_ORANGE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            lbl.setFont(new Font("Arial", Font.BOLD, 16));
            lbl.setForeground(ThemeManager.ACCENT_ORANGE);
            lbl.setOpaque(false);
            lbl.setPreferredSize(new Dimension(90, 38));
            return lbl;
        }

        private JButton actionBtn(String t, Color accent) {
            JButton b = new JButton(t) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? accent.brighter() : accent);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Arial", Font.BOLD, 12));
            b.setBorderPainted(false); b.setFocusPainted(false); b.setContentAreaFilled(false);
            b.setPreferredSize(new Dimension(165, 34));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        private JLabel wheelLabel(String txt, Color accent) {
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ThemeManager.getBg());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(accent);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            lbl.setFont(new Font("Arial", Font.BOLD, 30));
            lbl.setForeground(accent);
            lbl.setOpaque(false);
            lbl.setPreferredSize(new Dimension(64, 52));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return lbl;
        }

        private JButton wheelBtn(String t, Color accent) {
            JButton b = new JButton(t) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? accent.brighter() : accent);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Arial", Font.BOLD, 14));
            b.setBorderPainted(false); b.setFocusPainted(false); b.setContentAreaFilled(false);
            b.setPreferredSize(new Dimension(64, 26));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        private JLabel hint(String t) {
            JLabel l = new JLabel(t, SwingConstants.CENTER);
            l.setFont(new Font("Arial", Font.ITALIC, 9));
            l.setForeground(Color.GRAY);
            return l;
        }

        private void bounce(JLabel lbl) {
            final float[] s = {1.2f};
            javax.swing.Timer t = new javax.swing.Timer(16, null);
            t.addActionListener(e -> {
                s[0] -= 0.04f;
                if (s[0] <= 1f) { s[0] = 1f; t.stop(); }
                lbl.setFont(new Font("Arial", Font.BOLD, (int)(30 * s[0])));
            });
            t.start();
        }

        // ── API publique ──────────────────────────────────────────
        public String  getStartTime()    { return formatTime(startH, startM); }
        public String  getEndTime()      { return formatTime(endH, endM); }
        public int     getDureeHeures()  { return dureeH; }
        public int     getDureeMinutes() { return dureeM; }
        public String  getDureeStr()     { return String.valueOf(dureeH); }
        public boolean isValid()         { return (endH*60+endM) > (startH*60+startM); }
        public void addChangeListener(Runnable r) { listeners.add(r); }
    }

    // ================================================================
    // DURATION PICKER (compatibilite)
    // ================================================================
    public static class DurationPicker extends JPanel {
        private int value = 2;
        private JLabel lblVal;
        private final int min, max;
        private final List<Runnable> listeners = new ArrayList<>();

        public DurationPicker(int min, int max) {
            this.min = min; this.max = max;
            setLayout(new BorderLayout(8, 0));
            setOpaque(false);
            buildUI();
        }

        private void buildUI() {
            JButton bm = roundBtn("-", ThemeManager.ACCENT_RED);
            JButton bp = roundBtn("+", ThemeManager.ACCENT_GREEN);
            lblVal = new JLabel(value+"h", SwingConstants.CENTER) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color c = value<=1?ThemeManager.ACCENT_GREEN:value<=3?ThemeManager.ACCENT_BLUE:value<=6?ThemeManager.ACCENT_ORANGE:ThemeManager.ACCENT_RED;
                    g2.setColor(c); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    super.paintComponent(g);
                }
            };
            lblVal.setFont(new Font("Arial", Font.BOLD, 16));
            lblVal.setForeground(Color.WHITE); lblVal.setOpaque(false);
            lblVal.setPreferredSize(new Dimension(55, 36));
            JSlider s = new JSlider(min, max, value);
            s.setOpaque(false); s.setPaintTicks(true); s.setMajorTickSpacing(1); s.setSnapToTicks(true);
            Runnable u = () -> { lblVal.setText(value+"h"); lblVal.repaint(); s.setValue(value); listeners.forEach(Runnable::run); };
            bm.addActionListener(e -> { if(value>min){value--;u.run();} });
            bp.addActionListener(e -> { if(value<max){value++;u.run();} });
            s.addChangeListener(e  -> { value=s.getValue(); lblVal.setText(value+"h"); lblVal.repaint(); listeners.forEach(Runnable::run); });
            JPanel c2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); c2.setOpaque(false);
            c2.add(bm); c2.add(lblVal); c2.add(bp);
            add(c2, BorderLayout.WEST); add(s, BorderLayout.CENTER);
        }

        private JButton roundBtn(String t, Color bg) {
            JButton b = new JButton(t) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover()?bg.brighter():bg);
                    g2.fillOval(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g);
                }
            };
            b.setFont(new Font("Arial", Font.BOLD, 16)); b.setForeground(Color.WHITE);
            b.setBorderPainted(false); b.setFocusPainted(false); b.setContentAreaFilled(false);
            b.setPreferredSize(new Dimension(30,30)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        public int    getValue()       { return value; }
        public String getValueStr()    { return String.valueOf(value); }
        public void addChangeListener(Runnable r) { listeners.add(r); }
    }
}