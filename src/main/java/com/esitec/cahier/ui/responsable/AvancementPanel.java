package com.esitec.cahier.ui.responsable;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.utils.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AvancementPanel extends JPanel {

    private final Utilisateur responsable;
    private final CoursDAO coursDAO   = new CoursDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private DefaultTableModel model;
    private JTextField classeField;

    public AvancementPanel(Utilisateur responsable) {
        this.responsable = responsable;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setBackground(ThemeManager.getBg());

        JLabel lbl = new JLabel("Classe :");
        lbl.setForeground(ThemeManager.getText());
        lbl.setFont(new Font("Arial", Font.BOLD, 13));

        classeField = new JTextField(10);
        classeField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.ACCENT_TEAL, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JButton btnCharger = ThemeManager.btnTeal("📈 Voir avancement");

        top.add(lbl);
        top.add(classeField);
        top.add(btnCharger);
        add(top, BorderLayout.NORTH);

        String[] cols = {"Cours", "Volume H. prévu", "H. effectuées", "% Avancement", "Séances validées"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.getTableHeader().setBackground(ThemeManager.getTableHeader());
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnCharger.addActionListener(e -> chargerAvancement());
    }

    private void chargerAvancement() {
        model.setRowCount(0);
        String classe = classeField.getText().trim();
        if (classe.isEmpty()) { JOptionPane.showMessageDialog(this, "Entrez une classe."); return; }
        List<Cours> cours = coursDAO.listerParClasse(classe);
        for (Cours c : cours) {
            List<Seance> seances = seanceDAO.listerParCours(c.getId());
            int hEffectuees = seances.stream().mapToInt(Seance::getDuree).sum();
            long validees   = seances.stream().filter(s -> "VALIDEE".equals(s.getStatut())).count();
            int pct         = c.getVolumeHoraire() > 0 ? (hEffectuees * 100 / c.getVolumeHoraire()) : 0;
            model.addRow(new Object[]{c.getIntitule(), c.getVolumeHoraire() + "h", hEffectuees + "h", pct + "%", validees});
        }
        if (cours.isEmpty()) JOptionPane.showMessageDialog(this, "Aucun cours pour cette classe.");
    }
}
