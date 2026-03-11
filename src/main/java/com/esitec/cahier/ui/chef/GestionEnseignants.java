package com.esitec.cahier.ui.chef;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.utils.ThemeManager;
import com.esitec.cahier.ui.utils.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionEnseignants extends JPanel {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private DefaultTableModel modelEnseignants, modelComptes, modelCours;
    private final Utilisateur chef;

    public GestionEnseignants(Utilisateur chef) {
        this.chef = chef;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());
        initUI();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(ThemeManager.getBg());
        tabs.addTab("Enseignants", panelEnseignants());
        tabs.addTab("Comptes à valider", panelValidation());
        tabs.addTab("Assigner un cours", panelAssignerCours());
        add(tabs);
    }

    private JPanel panelEnseignants() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(ThemeManager.getBg());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Nom", "Prénom", "Email", "Statut"};
        modelEnseignants = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelEnseignants);
        styleTable(table);
        chargerEnseignants();

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        boutons.setBackground(ThemeManager.getBg());
        JButton btnAjouter    = ThemeManager.btnSuccess("➕ Ajouter enseignant");
        JButton btnRafraichir = ThemeManager.btnPrimary("🔄 Rafraîchir");
        btnAjouter.addActionListener(e -> ajouterEnseignant());
        btnRafraichir.addActionListener(e -> chargerEnseignants());
        boutons.add(btnAjouter);
        boutons.add(btnRafraichir);
        p.add(boutons, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void chargerEnseignants() {
        modelEnseignants.setRowCount(0);
        for (Utilisateur u : utilisateurDAO.listerParRole("ENSEIGNANT"))
            modelEnseignants.addRow(new Object[]{u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.isValide() ? "✅ Validé" : "❌ Non validé"});
    }

    private void ajouterEnseignant() {
        JTextField nom     = new JTextField();
        JTextField prenom  = new JTextField();
        JTextField email   = new JTextField();
        JPasswordField mdp = new JPasswordField();
        JPasswordField mdpConfirm = new JPasswordField();

        // Validations temps réel
        Validator.addRealTimeValidation(nom,    Validator::isNomValide,    "Lettres uniquement (2-50 car.)");
        Validator.addRealTimeValidation(prenom, Validator::isNomValide,    "Lettres uniquement (2-50 car.)");
        Validator.addRealTimeValidation(email,  Validator::isEmailValide,  "Format: nom@domaine.sn");

        Object[] fields = {
            "Nom :", nom, "Prénom :", prenom,
            "Email :", email,
            "Mot de passe :", mdp,
            "Confirmer mot de passe :", mdpConfirm
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Ajouter un enseignant", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String mdpStr     = new String(mdp.getPassword());
        String mdpConfStr = new String(mdpConfirm.getPassword());

        Validator.ValidationResult validation = Validator.validerEnseignant(
            nom.getText().trim(), prenom.getText().trim(), email.getText().trim(), mdpStr
        );

        var errors = new java.util.ArrayList<>(validation.errors());
        if (!mdpStr.equals(mdpConfStr)) errors.add("• Les mots de passe ne correspondent pas");

        if (!errors.isEmpty()) { Validator.showErrors(this, errors); return; }

        Utilisateur u = new Utilisateur(nom.getText().trim(), prenom.getText().trim(),
                                        email.getText().trim(), mdpStr, "ENSEIGNANT");
        u.setValide(true);

        if (utilisateurDAO.ajouter(u)) {
            Validator.showSuccess(this, "Enseignant " + u.getNomComplet() + " ajouté avec succès !");
            chargerEnseignants();
        } else {
            Validator.showErrors(this, java.util.List.of("• Cet email est déjà utilisé par un autre compte."));
        }
    }

    private JPanel panelValidation() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(ThemeManager.getBg());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Nom", "Prénom", "Email", "Rôle"};
        modelComptes = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelComptes);
        styleTable(table);
        chargerComptes();

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        boutons.setBackground(ThemeManager.getBg());
        JButton btnValider    = ThemeManager.btnSuccess("✅ Valider le compte");
        JButton btnRefuser    = ThemeManager.btnDanger("❌ Refuser");
        JButton btnRafraichir = ThemeManager.btnPrimary("🔄 Rafraîchir");

        btnValider.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { Validator.showErrors(this, java.util.List.of("• Sélectionnez un compte dans le tableau.")); return; }
            utilisateurDAO.validerCompte((int) modelComptes.getValueAt(row, 0));
            Validator.showSuccess(this, "Compte validé avec succès !");
            chargerComptes();
        });
        btnRefuser.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { Validator.showErrors(this, java.util.List.of("• Sélectionnez un compte dans le tableau.")); return; }
            JOptionPane.showMessageDialog(this, "Fonctionnalité de refus à implémenter.");
        });
        btnRafraichir.addActionListener(e -> chargerComptes());
        boutons.add(btnValider); boutons.add(btnRefuser); boutons.add(btnRafraichir);
        p.add(boutons, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void chargerComptes() {
        modelComptes.setRowCount(0);
        for (Utilisateur u : utilisateurDAO.listerNonValides())
            modelComptes.addRow(new Object[]{u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getRole()});
    }

    private JPanel panelAssignerCours() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(ThemeManager.getBg());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Intitulé", "Classe", "Volume H.", "Enseignant"};
        modelCours = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelCours);
        styleTable(table);
        chargerCours();

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        boutons.setBackground(ThemeManager.getBg());
        JButton btnAssigner   = ThemeManager.btnPurple("➕ Assigner un cours");
        JButton btnRafraichir = ThemeManager.btnPrimary("🔄 Rafraîchir");
        btnAssigner.addActionListener(e -> assignerCours());
        btnRafraichir.addActionListener(e -> chargerCours());

        JLabel infoLabel = new JLabel("  ℹ️ Seuls les enseignants validés sont disponibles.");
        infoLabel.setForeground(ThemeManager.ACCENT_GREEN);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        boutons.add(btnAssigner); boutons.add(btnRafraichir);
        north.add(boutons, BorderLayout.WEST);
        north.add(infoLabel, BorderLayout.SOUTH);
        p.add(north, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void chargerCours() {
        modelCours.setRowCount(0);
        for (Cours c : coursDAO.listerTous())
            modelCours.addRow(new Object[]{c.getId(), c.getIntitule(), c.getClasse(), c.getVolumeHoraire(), c.getNomEnseignant()});
    }

    private void assignerCours() {
        List<Utilisateur> enseignants = utilisateurDAO.listerEnseignantsDisponibles();
        if (enseignants.isEmpty()) {
            Validator.showErrors(this, java.util.List.of("• Aucun enseignant validé disponible.", "• Validez d'abord des comptes enseignants."));
            return;
        }

        JTextField intitule = new JTextField();
        JTextField classe   = new JTextField();
        JTextField volume   = new JTextField();
        JComboBox<Utilisateur> combo = new JComboBox<>(enseignants.toArray(new Utilisateur[0]));
        JLabel lblDispo = new JLabel("✅ " + enseignants.size() + " enseignant(s) disponible(s)");
        lblDispo.setForeground(ThemeManager.ACCENT_GREEN);
        lblDispo.setFont(new Font("Arial", Font.ITALIC, 11));

        Validator.addRealTimeValidation(volume, Validator::isVolumeHoraireValide, "Nombre entier entre 1 et 500");

        Object[] fields = {"Intitulé du cours :", intitule, "Classe :", classe, "Volume horaire (h) :", volume, "Enseignant :", combo, lblDispo};
        int result = JOptionPane.showConfirmDialog(this, fields, "Assigner un cours", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String intituleTxt = intitule.getText().trim();
        String classeTxt   = classe.getText().trim();
        String volumeTxt   = volume.getText().trim();
        Utilisateur ens    = (Utilisateur) combo.getSelectedItem();

        Validator.ValidationResult validation = Validator.validerCours(intituleTxt, classeTxt, volumeTxt);
        if (!validation.valid()) { Validator.showErrors(this, validation.errors()); return; }

        if (utilisateurDAO.enseignantDejaAssigneClasse(ens.getId(), classeTxt)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ " + ens.getNomComplet() + " a déjà un cours dans \"" + classeTxt + "\".\nContinuer quand même ?",
                "Avertissement", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        Cours c = new Cours(intituleTxt, Integer.parseInt(volumeTxt), ens.getId(), classeTxt);
        if (coursDAO.ajouter(c)) {
            Validator.showSuccess(this, "Cours \"" + intituleTxt + "\" assigné à " + ens.getNomComplet() + " !");
            chargerCours();
        } else {
            Validator.showErrors(this, java.util.List.of("• Erreur lors de l'enregistrement. Réessayez."));
        }
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setBackground(ThemeManager.getTableHeader());
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setBackground(ThemeManager.getCard());
        table.setForeground(ThemeManager.getText());
        table.setGridColor(ThemeManager.getBorder());
        table.setRowHeight(28);
    }
}
