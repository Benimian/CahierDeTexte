package com.esitec.cahier.ui.enseignant;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.utils.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class HistoriqueSeancesPanel extends JPanel {

    private final Utilisateur enseignant;
    private final SeanceDAO   seanceDAO = new SeanceDAO();
    private final CoursDAO    coursDAO  = new CoursDAO();

    private SortableTable     table;
    private DefaultTableModel model;
    private JLabel            lblTotal;
    private JComboBox<String> filtreCours;
    private List<Cours>       coursList;

    public HistoriqueSeancesPanel(Utilisateur enseignant) {
        this.enseignant = enseignant;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());
        buildModel();
        initUI();
        chargerDonnees(null);
    }

    private void buildModel() {
        String[] cols = {"#", "Cours", "Classe", "Date", "Debut", "Fin", "Duree", "Contenu", "Statut"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new SortableTable(model);
    }

    private void initUI() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────
    // Barre d'outils
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(ThemeManager.getCard());
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorder()),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        // Filtre par cours
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lblFiltre = new JLabel("Cours :");
        lblFiltre.setFont(new Font("Arial", Font.BOLD, 12));
        lblFiltre.setForeground(ThemeManager.getText());

        coursList = coursDAO.listerParEnseignant(enseignant.getId());
        String[] options = new String[coursList.size() + 1];
        options[0] = "Tous les cours";
        for (int i = 0; i < coursList.size(); i++)
            options[i+1] = coursList.get(i).getIntitule() + " — " + coursList.get(i).getClasse();

        filtreCours = new JComboBox<>(options);
        filtreCours.setBackground(ThemeManager.getBg());
        filtreCours.setForeground(ThemeManager.getText());
        filtreCours.setFont(new Font("Arial", Font.PLAIN, 12));
        filtreCours.setPreferredSize(new Dimension(260, 32));
        filtreCours.addActionListener(e -> appliquerFiltre());

        left.add(lblFiltre);
        left.add(filtreCours);
        bar.add(left, BorderLayout.WEST);

        // Recherche + refresh
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JTextField searchBar = table.buildSearchBar("Rechercher...");
        searchBar.setPreferredSize(new Dimension(200, 32));

        AnimationManager.AnimatedButton btnRefresh = new AnimationManager.AnimatedButton(
            "Actualiser",
            ThemeManager.ACCENT_BLUE, new Color(50, 90, 170), new Color(20, 50, 110));
        btnRefresh.setPreferredSize(new Dimension(110, 32));
        btnRefresh.addActionListener(e -> rafraichir());

        right.add(searchBar);
        right.add(btnRefresh);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────
    // Tableau
    // ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        table.setRowHeight(38);

        // Largeurs des colonnes
        int[] widths = {35, 160, 80, 95, 65, 65, 60, 200, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer badge statut — colonne 8
        table.getColumnModel().getColumn(8).setCellRenderer(
            new StatutBadgeRenderer());

        // Renderer contenu tronque avec tooltip — colonne 7
        table.getColumnModel().getColumn(7).setCellRenderer(
            new ContenuRenderer());

        // Menu clic droit
        table.addContextAction("Voir le contenu complet", row -> voirDetail(row));
        table.addContextAction("Copier la date",          row -> {
            Object val = model.getValueAt(row, 3);
            if (val != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(val.toString()), null);
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(ThemeManager.getBg());
        return sp;
    }

    // ─────────────────────────────────────────────────────────────────
    // Pied de page
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        footer.setBackground(ThemeManager.getCard());
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getBorder()));

        lblTotal = new JLabel("0 seance(s)");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotal.setForeground(ThemeManager.getText());

        footer.add(lblTotal);
        return footer;
    }

    // ─────────────────────────────────────────────────────────────────
    // Chargement des donnees
    // ─────────────────────────────────────────────────────────────────
    private void chargerDonnees(Integer coursIdFiltre) {
        model.setRowCount(0);
        List<Cours> mesCours = coursDAO.listerParEnseignant(enseignant.getId());
        int num = 1;

        for (Cours cours : mesCours) {
            if (coursIdFiltre != null && cours.getId() != coursIdFiltre) continue;
            List<Seance> seances = seanceDAO.listerParCours(cours.getId());
            for (Seance s : seances) {
                String fin = calculerFin(s.getHeure(), s.getDuree());
                model.addRow(new Object[]{
                    num++,
                    cours.getIntitule(),
                    cours.getClasse(),
                    s.getDate(),
                    s.getHeure(),
                    fin,
                    s.getDuree() + "h",
                    s.getContenu(),
                    s.getStatut()   // "EN_ATTENTE", "VALIDEE", "REJETEE"
                });
            }
        }

        int total = model.getRowCount();
        lblTotal.setText(total + " seance" + (total > 1 ? "s" : ""));
    }

    private void appliquerFiltre() {
        int idx = filtreCours.getSelectedIndex();
        if (idx == 0) chargerDonnees(null);
        else          chargerDonnees(coursList.get(idx - 1).getId());
    }

    public void rafraichir()      { appliquerFiltre(); }
    public int  getSeanceCount()  { return model != null ? model.getRowCount() : 0; }

    private void voirDetail(int rowIdx) {
        Object contenu = model.getValueAt(rowIdx, 7);
        Object date    = model.getValueAt(rowIdx, 3);
        Object cours   = model.getValueAt(rowIdx, 1);
        JOptionPane.showMessageDialog(this,
            "<html><b>" + cours + " — " + date + "</b><br><br>"
            + "<p style='width:350px'>"
            + (contenu != null ? contenu.toString().replace("\n", "<br>") : "(vide)")
            + "</p></html>",
            "Contenu de la seance", JOptionPane.INFORMATION_MESSAGE);
    }

    private String calculerFin(String heure, int duree) {
        try {
            String[] parts = heure.split(":");
            int h = Integer.parseInt(parts[0]) + duree;
            int m = Integer.parseInt(parts[1]);
            return String.format("%02d:%02d", h % 24, m);
        } catch (Exception e) { return "—"; }
    }

    // ─────────────────────────────────────────────────────────────────
    // Renderers internes
    // ─────────────────────────────────────────────────────────────────

    /** Badge arrondi colore selon le statut de la seance */
    private class StatutBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean foc, int row, int col) {

            String statut = value != null ? value.toString() : "EN_ATTENTE";

            Color bgColor, fgColor, borderColor;
            String label;
            switch (statut) {
                case "VALIDEE" -> {
                    bgColor     = new Color(39,  174, 96,  45);
                    borderColor = ThemeManager.ACCENT_GREEN;
                    fgColor     = ThemeManager.ACCENT_GREEN;
                    label       = "Validee";
                }
                case "REJETEE" -> {
                    bgColor     = new Color(231, 76,  60,  45);
                    borderColor = ThemeManager.ACCENT_RED;
                    fgColor     = ThemeManager.ACCENT_RED;
                    label       = "Rejetee";
                }
                default -> {
                    bgColor     = new Color(230, 126, 34,  45);
                    borderColor = ThemeManager.ACCENT_ORANGE;
                    fgColor     = ThemeManager.ACCENT_ORANGE;
                    label       = "En attente";
                }
            }

            final Color bg  = bgColor;
            final Color bd  = borderColor;
            final String lbl = label;
            final Color fg  = fgColor;

            JLabel badge = new JLabel(lbl, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(3, 4, getWidth()-6, getHeight()-8, 10, 10);
                    g2.setColor(bd);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(3, 4, getWidth()-6, getHeight()-8, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Arial", Font.BOLD, 11));
            badge.setForeground(fg);
            badge.setOpaque(false);
            if (sel) badge.setBackground(new Color(30, 60, 114, 40));
            return badge;
        }
    }

    /** Contenu tronque avec tooltip sur survol */
    private class ContenuRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean foc, int row, int col) {
            Component cell = super.getTableCellRendererComponent(
                t, value, sel, foc, row, col);
            String full    = value != null ? value.toString() : "";
            String display = full.length() > 45 ? full.substring(0, 42) + "..." : full;
            ((JLabel) cell).setText(display);
            ((JLabel) cell).setToolTipText(
                "<html><p style='width:300px'>" + full + "</p></html>");
            ((JLabel) cell).setBorder(
                BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!sel) {
                cell.setBackground(row % 2 == 0
                    ? ThemeManager.getCard() : ThemeManager.getTableAlt());
                cell.setForeground(ThemeManager.getText());
            }
            return cell;
        }
    }
}
