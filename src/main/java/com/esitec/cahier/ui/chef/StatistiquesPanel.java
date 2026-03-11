package com.esitec.cahier.ui.chef;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.ui.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class StatistiquesPanel extends JPanel {

    // ── Données ───────────────────────────────────────────────────────
    private int nbEnseignants, nbResponsables, nbCours, nbSeances, nbValidees, nbRejetees, nbEnAttente;
    private Map<String, Integer> seancesParClasse = new LinkedHashMap<>();
    private Map<String, Integer> avancementParCours = new LinkedHashMap<>();

    public StatistiquesPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getBg());
        chargerDonnees();
        buildUI();
    }

    // ════════════════════════════════════════════════════════════════════
    // CHARGEMENT DES DONNÉES
    // ════════════════════════════════════════════════════════════════════
    private void chargerDonnees() {
        UtilisateurDAO uDAO = new UtilisateurDAO();
        CoursDAO cDAO       = new CoursDAO();
        SeanceDAO sDAO      = new SeanceDAO();

        nbEnseignants  = uDAO.listerParRole("ENSEIGNANT").size();
        nbResponsables = uDAO.listerParRole("RESPONSABLE").size();

        List<Cours> cours = cDAO.listerTous();
        nbCours = cours.size();

        Map<String, Integer> classeCount = new LinkedHashMap<>();

        for (Cours c : cours) {
            List<Seance> seances = sDAO.listerParCours(c.getId());
            nbSeances  += seances.size();
            nbValidees += (int) seances.stream().filter(s -> "VALIDEE".equals(s.getStatut())).count();
            nbRejetees += (int) seances.stream().filter(s -> "REJETEE".equals(s.getStatut())).count();

            // Séances par classe
            String cl = c.getClasse() != null ? c.getClasse() : "Inconnue";
            classeCount.merge(cl, seances.size(), Integer::sum);

            // Avancement par cours (% volume horaire effectué)
            if (c.getVolumeHoraire() > 0) {
                int hEffectuees = seances.stream().mapToInt(Seance::getDuree).sum();
                int pct = Math.min(100, hEffectuees * 100 / c.getVolumeHoraire());
                String label = c.getIntitule().length() > 18
                    ? c.getIntitule().substring(0, 16) + "…" : c.getIntitule();
                avancementParCours.put(label, pct);
            }
        }

        nbEnAttente = nbSeances - nbValidees - nbRejetees;
        seancesParClasse = classeCount;
    }

    // ════════════════════════════════════════════════════════════════════
    // CONSTRUCTION DE L'UI
    // ════════════════════════════════════════════════════════════════════
    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── Contenu scrollable ────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getBg());
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Ligne 1 — KPI cards
        content.add(buildKpiRow());
        content.add(Box.createVerticalStrut(20));

        // Ligne 2 — Donut + Barres horizontales
        content.add(buildChartsRow());
        content.add(Box.createVerticalStrut(20));

        // Ligne 3 — Avancement par cours
        content.add(buildAvancementSection());
        content.add(Box.createVerticalStrut(20));

        // Ligne 4 — Tableau récapitulatif
        content.add(buildTableauSection());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20, 40, 80));
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel titre = new JLabel("📊  Tableau de Bord Statistiques");
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setForeground(Color.WHITE);
        p.add(titre, BorderLayout.WEST);

        JButton btnRafraichir = new JButton("🔄 Actualiser");
        btnRafraichir.setBackground(new Color(255, 255, 255, 40));
        btnRafraichir.setForeground(Color.WHITE);
        btnRafraichir.setBorderPainted(false);
        btnRafraichir.setFont(new Font("Arial", Font.BOLD, 12));
        btnRafraichir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRafraichir.addActionListener(e -> rafraichir());
        p.add(btnRafraichir, BorderLayout.EAST);

        JLabel sub = new JLabel("Vue globale en temps réel de l'activité pédagogique");
        sub.setFont(new Font("Arial", Font.ITALIC, 11));
        sub.setForeground(new Color(160, 185, 230));
        p.add(sub, BorderLayout.SOUTH);

        return p;
    }

    // ── KPI Cards ─────────────────────────────────────────────────────
    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 6, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        row.add(kpiCard("👨‍🏫", "Enseignants",   String.valueOf(nbEnseignants),  new Color(52, 152, 219)));
        row.add(kpiCard("👤",   "Responsables",  String.valueOf(nbResponsables), new Color(155, 89, 182)));
        row.add(kpiCard("📚",   "Cours",         String.valueOf(nbCours),        new Color(22, 160, 133)));
        row.add(kpiCard("📝",   "Séances",       String.valueOf(nbSeances),      new Color(230, 126, 34)));
        row.add(kpiCard("✅",   "Validées",      String.valueOf(nbValidees),     new Color(39, 174, 96)));
        row.add(kpiCard("⏳",   "En attente",    String.valueOf(nbEnAttente),    new Color(241, 196, 15)));

        return row;
    }

    private JPanel kpiCard(String icon, String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fond
                g2.setColor(ThemeManager.getCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Barre accent en haut
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 5, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 12, 12, 12));

        JLabel lblIcon = new JLabel(icon + "  " + label, SwingConstants.LEFT);
        lblIcon.setFont(new Font("Arial", Font.PLAIN, 11));
        lblIcon.setForeground(Color.GRAY);

        JLabel lblVal = new JLabel(value, SwingConstants.LEFT);
        lblVal.setFont(new Font("Arial", Font.BOLD, 30));
        lblVal.setForeground(accent);

        card.add(lblIcon, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    // ── Graphiques ────────────────────────────────────────────────────
    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        row.add(buildDonutCard());
        row.add(buildBarCard());
        return row;
    }

    // Donut chart statut séances
    private JPanel buildDonutCard() {
        JPanel card = buildCard("Répartition des séances");
        DonutChart donut = new DonutChart(
            new String[]{"Validées", "En attente", "Rejetées"},
            new int[]{nbValidees, nbEnAttente, nbRejetees},
            new Color[]{new Color(39, 174, 96), new Color(241, 196, 15), new Color(231, 76, 60)}
        );
        donut.setPreferredSize(new Dimension(200, 200));
        card.add(donut, BorderLayout.CENTER);
        return card;
    }

    // Barres horizontales séances par classe
    private JPanel buildBarCard() {
        JPanel card = buildCard("Séances par classe");
        if (seancesParClasse.isEmpty()) {
            JLabel lbl = new JLabel("Aucune donnée", SwingConstants.CENTER);
            lbl.setForeground(Color.GRAY);
            card.add(lbl, BorderLayout.CENTER);
        } else {
            HBarChart bars = new HBarChart(seancesParClasse, new Color(52, 152, 219));
            card.add(bars, BorderLayout.CENTER);
        }
        return card;
    }

    // ── Avancement par cours ──────────────────────────────────────────
    private JPanel buildAvancementSection() {
        JPanel card = buildCard("Avancement des cours (% volume horaire)");
        if (avancementParCours.isEmpty()) {
            JLabel lbl = new JLabel("Aucun cours enregistré", SwingConstants.CENTER);
            lbl.setForeground(Color.GRAY);
            card.add(lbl, BorderLayout.CENTER);
            return card;
        }

        JPanel barsPanel = new JPanel();
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));
        barsPanel.setOpaque(false);
        barsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (Map.Entry<String, Integer> entry : avancementParCours.entrySet()) {
            barsPanel.add(buildProgressRow(entry.getKey(), entry.getValue()));
            barsPanel.add(Box.createVerticalStrut(8));
        }

        card.add(barsPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildProgressRow(String label, int pct) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("Arial", Font.BOLD, 11));
        lblName.setForeground(ThemeManager.getText());
        lblName.setPreferredSize(new Dimension(160, 20));

        JLabel lblPct = new JLabel(pct + "%");
        lblPct.setFont(new Font("Arial", Font.BOLD, 11));
        lblPct.setForeground(pct >= 80 ? ThemeManager.ACCENT_GREEN :
                             pct >= 40 ? ThemeManager.ACCENT_ORANGE : ThemeManager.ACCENT_RED);
        lblPct.setPreferredSize(new Dimension(40, 20));

        ProgressBar bar = new ProgressBar(pct,
            pct >= 80 ? new Color(39, 174, 96) :
            pct >= 40 ? new Color(243, 156, 18) : new Color(231, 76, 60));

        row.add(lblName, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(lblPct, BorderLayout.EAST);
        return row;
    }

    // ── Tableau récapitulatif ─────────────────────────────────────────
    private JPanel buildTableauSection() {
        JPanel card = buildCard("Récapitulatif global");

        String[][] data = {
            {"Total enseignants",  String.valueOf(nbEnseignants),  nbEnseignants > 0 ? "✅" : "⚠️"},
            {"Total responsables", String.valueOf(nbResponsables), nbResponsables > 0 ? "✅" : "⚠️"},
            {"Total cours",        String.valueOf(nbCours),        nbCours > 0 ? "✅" : "⚠️"},
            {"Total séances",      String.valueOf(nbSeances),      nbSeances > 0 ? "✅" : "⚠️"},
            {"Séances validées",   nbValidees + " / " + nbSeances, nbSeances > 0 ? String.format("%.0f%%", nbSeances > 0 ? (double) nbValidees / nbSeances * 100 : 0) : "-"},
            {"Séances rejetées",   String.valueOf(nbRejetees),     nbRejetees == 0 ? "✅" : "⚠️"},
            {"En attente",         String.valueOf(nbEnAttente),    nbEnAttente == 0 ? "✅" : "🔔"},
        };

        String[] cols = {"Indicateur", "Valeur", "Statut"};
        JTable table = new JTable(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setRowHeight(30);
        table.setBackground(ThemeManager.getCard());
        table.setForeground(ThemeManager.getText());
        table.getTableHeader().setBackground(new Color(20, 40, 80));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setGridColor(ThemeManager.getBorder());
        table.setShowGrid(true);

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ── Utilitaires ───────────────────────────────────────────────────
    private JPanel buildCard(String titre) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 13));
        lblTitre.setForeground(new Color(20, 40, 80));
        lblTitre.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorder()));
        card.add(lblTitre, BorderLayout.NORTH);
        return card;
    }

    private void rafraichir() {
        nbEnseignants = nbResponsables = nbCours = nbSeances = nbValidees = nbRejetees = nbEnAttente = 0;
        seancesParClasse.clear();
        avancementParCours.clear();
        removeAll();
        chargerDonnees();
        buildUI();
        revalidate();
        repaint();
    }

    // ════════════════════════════════════════════════════════════════════
    // COMPOSANTS GRAPHIQUES CUSTOM
    // ════════════════════════════════════════════════════════════════════

    /** Donut chart */
    static class DonutChart extends JPanel {
        private final String[] labels;
        private final int[] values;
        private final Color[] colors;

        DonutChart(String[] labels, int[] values, Color[] colors) {
            this.labels = labels; this.values = values; this.colors = colors;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = Arrays.stream(values).sum();
            if (total == 0) {
                g2.setColor(Color.GRAY);
                g2.drawString("Aucune donnée", getWidth() / 2 - 40, getHeight() / 2);
                g2.dispose(); return;
            }

            int size    = Math.min(getWidth(), getHeight()) - 80;
            int x       = (getWidth() - size) / 2;
            int y       = 10;
            double start = -90;

            for (int i = 0; i < values.length; i++) {
                double arc = 360.0 * values[i] / total;
                g2.setColor(colors[i]);
                g2.fill(new Arc2D.Double(x, y, size, size, start, arc, Arc2D.PIE));
                start += arc;
            }

            // Trou donut
            g2.setColor(ThemeManager.getCard());
            int hole = size / 3;
            g2.fillOval(x + (size - hole) / 2, y + (size - hole) / 2, hole, hole);

            // Total au centre
            g2.setColor(ThemeManager.getText());
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            String totalStr = String.valueOf(total);
            FontMetrics fm  = g2.getFontMetrics();
            g2.drawString(totalStr,
                x + size / 2 - fm.stringWidth(totalStr) / 2,
                y + size / 2 + fm.getAscent() / 2 - 2);

            // Légende
            int ly = y + size + 14;
            int lx = 10;
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(colors[i]);
                g2.fillRoundRect(lx, ly, 12, 12, 4, 4);
                g2.setColor(ThemeManager.getText());
                String txt = labels[i] + " (" + values[i] + ")";
                g2.drawString(txt, lx + 16, ly + 10);
                lx += g2.getFontMetrics().stringWidth(txt) + 28;
            }

            g2.dispose();
        }
    }

    /** Barres horizontales */
    static class HBarChart extends JPanel {
        private final Map<String, Integer> data;
        private final Color barColor;

        HBarChart(Map<String, Integer> data, Color barColor) {
            this.data = data; this.barColor = barColor;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int maxVal  = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            int rows    = data.size();
            int rowH    = (getHeight() - 20) / Math.max(rows, 1);
            int labelW  = 90;
            int barArea = getWidth() - labelW - 50;
            int y       = 10;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int barW = maxVal > 0 ? barArea * entry.getValue() / maxVal : 0;

                // Label
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                g2.setColor(ThemeManager.getText());
                g2.drawString(entry.getKey(), 5, y + rowH / 2 + 4);

                // Fond barre
                g2.setColor(ThemeManager.getBorder());
                g2.fillRoundRect(labelW, y + 4, barArea, rowH - 10, 8, 8);

                // Barre colorée
                g2.setColor(barColor);
                g2.fillRoundRect(labelW, y + 4, barW, rowH - 10, 8, 8);

                // Valeur
                g2.setColor(ThemeManager.getText());
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.drawString(String.valueOf(entry.getValue()), labelW + barW + 6, y + rowH / 2 + 4);

                y += rowH;
            }
            g2.dispose();
        }
    }

    /** Barre de progression animée */
    static class ProgressBar extends JPanel {
        private final int pct;
        private final Color color;
        private int displayed = 0;
        private javax.swing.Timer anim;

        ProgressBar(int pct, Color color) {
            this.pct = pct; this.color = color;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 20));
            // Animation
            anim = new javax.swing.Timer(12, e -> {
                if (displayed < pct) { displayed += 2; repaint(); }
                else { displayed = pct; ((javax.swing.Timer) e.getSource()).stop(); repaint(); }
            });
            anim.start();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = 14;
            int y = (getHeight() - h) / 2;

            // Fond
            g2.setColor(ThemeManager.getBorder());
            g2.fillRoundRect(0, y, getWidth(), h, h, h);

            // Remplissage
            int w = (int)(getWidth() * displayed / 100.0);
            if (w > 0) {
                GradientPaint gp = new GradientPaint(0, 0, color.brighter(), w, 0, color.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, y, w, h, h, h);
            }
            g2.dispose();
        }
    }
}
