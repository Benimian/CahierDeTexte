package com.esitec.cahier.ui.responsable;

import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.utils.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ValidationPanel extends JPanel {

    private final Utilisateur responsable;
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private DefaultTableModel model;
    private JTable table;
    private JTextField classeField;

    public ValidationPanel(Utilisateur responsable) {
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
            BorderFactory.createLineBorder(ThemeManager.ACCENT_PURPLE, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JButton btnCharger = ThemeManager.btnPrimary("🔍 Charger");
        JButton btnValider = ThemeManager.btnSuccess("✅ Valider");
        JButton btnRejeter = ThemeManager.btnDanger("❌ Rejeter");

        top.add(lbl);
        top.add(classeField);
        top.add(btnCharger);
        top.add(btnValider);
        top.add(btnRejeter);
        add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Cours", "Date", "Heure", "Durée(h)", "Contenu", "Statut"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(ThemeManager.getTableHeader());
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnCharger.addActionListener(e -> chargerSeances());
        btnValider.addActionListener(e -> valider());
        btnRejeter.addActionListener(e -> rejeter());
    }

    private void chargerSeances() {
        model.setRowCount(0);
        String classe = classeField.getText().trim();
        if (classe.isEmpty()) { JOptionPane.showMessageDialog(this, "Entrez une classe."); return; }
        List<Seance> seances = seanceDAO.listerParClasse(classe);
        for (Seance s : seances) {
            model.addRow(new Object[]{s.getId(), s.getIntituleCours(), s.getDate(),
                s.getHeure(), s.getDuree(), s.getContenu(), s.getStatut()});
        }
        if (seances.isEmpty()) JOptionPane.showMessageDialog(this, "Aucune séance pour cette classe.");
    }

    private void valider() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une séance."); return; }
        seanceDAO.valider((int) model.getValueAt(row, 0));
        JOptionPane.showMessageDialog(this, "✅ Séance validée !");
        chargerSeances();
    }

    private void rejeter() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une séance."); return; }
        String commentaire = JOptionPane.showInputDialog(this, "Motif du rejet :");
        if (commentaire != null && !commentaire.isEmpty()) {
            seanceDAO.rejeter((int) model.getValueAt(row, 0), commentaire);
            JOptionPane.showMessageDialog(this, "❌ Séance rejetée.");
            chargerSeances();
        }
    }
}
