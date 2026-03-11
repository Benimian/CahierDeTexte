package com.esitec.cahier.ui.enseignant;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.utils.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class AjouterSeancePanel extends JPanel {

    private final Utilisateur         enseignant;
    private final EnseignantDashboard dashboard;
    private final CoursDAO            coursDAO  = new CoursDAO();
    private final SeanceDAO           seanceDAO = new SeanceDAO();

    private JComboBox<Cours>               comboCours;
    private DateTimePicker.DatePicker      datePicker;
    private DateTimePicker.TimeRangePicker timeRangePicker;
    private JTextArea                      contenuArea, observationsArea;
    private JLabel                         lblContenuCount, lblPreview;
    private JProgressBar                   progressBar;
    private AutoSave                       autoSave;

    public AjouterSeancePanel(Utilisateur enseignant, EnseignantDashboard dashboard) {
        this.enseignant = enseignant;
        this.dashboard  = dashboard;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());
        initUI();
        setupShortcuts();
    }

    private void initUI() {
        List<Cours> cours = coursDAO.listerParEnseignant(enseignant.getId());

        comboCours       = buildComboCours(cours);
        datePicker       = new DateTimePicker.DatePicker();
        timeRangePicker  = new DateTimePicker.TimeRangePicker();
        contenuArea      = buildTextArea(4);
        observationsArea = buildTextArea(3);
        lblContenuCount  = new JLabel("0 / 500");
        lblPreview       = new JLabel(" ");
        progressBar      = new JProgressBar(0, 3);

        // Auto-save : register AVANT restore
        autoSave = new AutoSave("seance_" + enseignant.getId(), 30);
        autoSave.register("contenu", contenuArea);
        autoSave.register("observations", observationsArea);

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(ThemeManager.getBg());
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        wrapper.add(buildProgressSection(), BorderLayout.NORTH);
        wrapper.add(buildFormCard(cours),   BorderLayout.CENTER);
        wrapper.add(buildFooterBar(),       BorderLayout.SOUTH);

        add(new JScrollPane(wrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // Listeners
        comboCours.addActionListener(e -> updateProgress());
        datePicker.addChangeListener(this::updateProgress);
        timeRangePicker.addChangeListener(this::updateProgress);
        timeRangePicker.addChangeListener(this::updatePreview);
        datePicker.addChangeListener(this::updatePreview);
        contenuArea.getDocument().addDocumentListener(docListener(() -> {
            updateContenuCount();
            updateProgress();
        }));

        updatePreview();
        updateProgress();

        // Restore brouillon APRES enregistrement des champs
        boolean restored = autoSave.restore();
        if (restored) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "Un brouillon a ete restaure automatiquement.",
                    "Brouillon restaure", JOptionPane.INFORMATION_MESSAGE));
        }
        if (cours.isEmpty()) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "Aucun cours ne vous est assigne.",
                    "Info", JOptionPane.INFORMATION_MESSAGE));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Barre de progression
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildProgressSection() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JPanel labels = new JPanel(new GridLayout(1, 3));
        labels.setOpaque(false);
        String[] etapes = {"(1) Cours & Date", "(2) Horaires", "(3) Contenu"};
        Color[]  colors = {ThemeManager.ACCENT_BLUE, ThemeManager.ACCENT_ORANGE, ThemeManager.ACCENT_GREEN};
        for (int i = 0; i < 3; i++) {
            JLabel lbl = new JLabel(etapes[i], SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 11));
            lbl.setForeground(colors[i]);
            labels.add(lbl);
        }

        progressBar.setStringPainted(true);
        progressBar.setString("Formulaire vide");
        progressBar.setPreferredSize(new Dimension(0, 18));
        progressBar.setBackground(ThemeManager.getBorder());
        progressBar.setForeground(ThemeManager.ACCENT_GREEN);
        progressBar.setBorderPainted(false);

        p.add(labels,      BorderLayout.NORTH);
        p.add(progressBar, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────
    // Carte formulaire avec coins arrondis
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildFormCard(List<Cours> cours) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCard());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.setColor(ThemeManager.getBorder());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        int row = 0;

        // Section 1 : Cours & Date
        row = addSectionHeader(card, row, "1 - Cours & Date", ThemeManager.ACCENT_BLUE);
        row = addRow(card, row, "Cours :", comboCours);
        row = addRow(card, row, "Date :", datePicker);

        // Section 2 : Horaires
        row = addSectionHeader(card, row, "2 - Horaires (debut / fin / duree)", ThemeManager.ACCENT_ORANGE);
        row = addRow(card, row, "Creneaux :", timeRangePicker);

        // Recap horaire
        GridBagConstraints gc = gbc(0, row++, 2);
        gc.insets = new Insets(2, 8, 14, 8);
        lblPreview.setFont(new Font("Arial", Font.BOLD, 12));
        lblPreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 60, 114, 60), 1, true),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        card.add(lblPreview, gc);

        // Section 3 : Contenu
        row = addSectionHeader(card, row, "3 - Contenu pedagogique", ThemeManager.ACCENT_GREEN);

        GridBagConstraints gl = gbc(0, row, 1); gl.weightx = 0.25;
        card.add(fieldLabel("Contenu :"), gl);
        GridBagConstraints gr = gbc(1, row++, 1); gr.weightx = 0.75;
        card.add(styledScrollPane(contenuArea), gr);

        GridBagConstraints gcnt = gbc(1, row++, 1);
        gcnt.insets = new Insets(2, 10, 8, 8);
        lblContenuCount.setFont(new Font("Arial", Font.ITALIC, 10));
        lblContenuCount.setForeground(Color.GRAY);
        card.add(lblContenuCount, gcnt);

        gl = gbc(0, row, 1); gl.weightx = 0.25;
        card.add(fieldLabel("Observations :"), gl);
        gr = gbc(1, row, 1); gr.weightx = 0.75;
        card.add(styledScrollPane(observationsArea), gr);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────
    // Pied de page
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildFooterBar() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(autoSave.getIndicator());

        JLabel hints = new JLabel("Ctrl+S : Enregistrer  |  Ctrl+R : Effacer  |  Ctrl+P : Profil");
        hints.setFont(new Font("Arial", Font.ITALIC, 10));
        hints.setForeground(Color.GRAY);
        hints.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        AnimationManager.AnimatedButton btnClear = new AnimationManager.AnimatedButton(
            "Effacer  Ctrl+R",
            new Color(100, 110, 130), new Color(120, 130, 155), new Color(80, 88, 108));
        btnClear.setPreferredSize(new Dimension(155, 40));
        btnClear.addActionListener(e -> confirmerEffacement());

        AnimationManager.AnimatedButton btnSave = new AnimationManager.AnimatedButton(
            "Enregistrer  Ctrl+S",
            ThemeManager.ACCENT_GREEN, new Color(50, 200, 110), new Color(28, 140, 75));
        btnSave.setPreferredSize(new Dimension(175, 40));
        btnSave.addActionListener(e -> enregistrerSeance());

        right.add(btnClear);
        right.add(btnSave);

        footer.add(left,  BorderLayout.WEST);
        footer.add(hints, BorderLayout.CENTER);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    // ─────────────────────────────────────────────────────────────────
    // Logique metier
    // ─────────────────────────────────────────────────────────────────
    private void enregistrerSeance() {
        List<String> errors = validateForm();
        if (!errors.isEmpty()) { Validator.showErrors(this, errors); return; }

        Cours c = (Cours) comboCours.getSelectedItem();
        Seance s = new Seance(
            c.getId(),
            datePicker.getValue(),
            timeRangePicker.getStartTime(),
            timeRangePicker.getDureeHeures(),
            contenuArea.getText().trim(),
            observationsArea.getText().trim()
        );

        if (seanceDAO.ajouter(s)) {
            String dureeStr = timeRangePicker.getDureeHeures() + "h"
                + (timeRangePicker.getDureeMinutes() > 0
                    ? timeRangePicker.getDureeMinutes() + "min" : "");
            String msg = "Seance enregistree ! "
                + "Date: " + datePicker.getValue()
                + "  Debut: " + timeRangePicker.getStartTime()
                + " -> Fin: " + timeRangePicker.getEndTime()
                + "  Duree: " + dureeStr;
            Validator.showSuccess(this, msg);
            autoSave.clear();
            effacer();
            if (dashboard != null) dashboard.updateHistoriqueCount();
        } else {
            Validator.showErrors(this, List.of("Erreur lors de l'enregistrement. Reessayez."));
        }
    }

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();
        if (comboCours.getSelectedItem() == null)
            errors.add("Selectionnez un cours.");
        if (!timeRangePicker.isValid())
            errors.add("L'heure de fin doit etre apres l'heure de debut.");
        String contenu = contenuArea.getText().trim();
        if (contenu.length() < 5)
            errors.add("Le contenu doit faire au moins 5 caracteres.");
        if (contenu.length() > 500)
            errors.add("Le contenu depasse 500 caracteres (" + contenu.length() + " saisis).");
        return errors;
    }

    private void confirmerEffacement() {
        if (contenuArea.getText().isBlank() && observationsArea.getText().isBlank()) {
            effacer(); return;
        }
        int rep = JOptionPane.showConfirmDialog(this,
            "Effacer le formulaire ? Le brouillon sera supprime.",
            "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) { autoSave.clear(); effacer(); }
    }

    private void effacer() {
        contenuArea.setText("");
        observationsArea.setText("");
        updateProgress();
    }

    // ─────────────────────────────────────────────────────────────────
    // Indicateurs visuels
    // ─────────────────────────────────────────────────────────────────
    private void updateProgress() {
        int score = 0;
        if (comboCours.getSelectedItem() != null)       score++;
        if (timeRangePicker.isValid())                  score++;
        if (contenuArea.getText().trim().length() >= 5) score++;

        progressBar.setValue(score);
        Color c = score == 0 ? Color.GRAY
                : score == 1 ? ThemeManager.ACCENT_ORANGE
                : score == 2 ? ThemeManager.ACCENT_BLUE
                :              ThemeManager.ACCENT_GREEN;
        progressBar.setForeground(c);
        progressBar.setString(
            score == 3 ? "Formulaire complet - pret a enregistrer !" :
            score == 2 ? "Presque complet..." :
            score == 1 ? "En cours de remplissage..." :
                         "Formulaire vide");
    }

    private void updatePreview() {
        if (!timeRangePicker.isValid()) {
            lblPreview.setText("Heure de fin invalide -- reglez les creneaux");
            lblPreview.setForeground(ThemeManager.ACCENT_RED);
        } else {
            int dh = timeRangePicker.getDureeHeures();
            int dm = timeRangePicker.getDureeMinutes();
            String dureeStr = dh + "h" + (dm > 0 ? dm + "min" : "");
            lblPreview.setText(
                "Date : " + datePicker.getValue()
                + "    Debut : " + timeRangePicker.getStartTime()
                + "  ->  Fin : " + timeRangePicker.getEndTime()
                + "    Duree : " + dureeStr);
            lblPreview.setForeground(ThemeManager.ACCENT_BLUE);
        }
    }

    private void updateContenuCount() {
        int len = contenuArea.getText().length();
        lblContenuCount.setText(len + " / 500");
        lblContenuCount.setForeground(
            len > 500 ? ThemeManager.ACCENT_RED :
            len >= 5  ? ThemeManager.ACCENT_GREEN : Color.GRAY);
    }

    // ─────────────────────────────────────────────────────────────────
    // Raccourcis clavier
    // ─────────────────────────────────────────────────────────────────
    private void setupShortcuts() {
        bindKey(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, "save",   e -> enregistrerSeance());
        bindKey(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, "clear",  e -> confirmerEffacement());
        bindKey(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, "profil", e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (parent != null) new ProfilDialog(parent).setVisible(true);
        });
    }

    private void bindKey(int key, int mod, String name, java.awt.event.ActionListener action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, mod), name);
        getActionMap().put(name, new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { action.actionPerformed(e); }
        });
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers UI
    // ─────────────────────────────────────────────────────────────────
    private JComboBox<Cours> buildComboCours(List<Cours> cours) {
        JComboBox<Cours> combo = new JComboBox<>(cours.toArray(new Cours[0]));
        combo.setBackground(ThemeManager.getBg());
        combo.setForeground(ThemeManager.getText());
        combo.setFont(new Font("Arial", Font.PLAIN, 13));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Cours c) {
                    setText(c.getIntitule() + "   [" + c.getClasse() + "]");
                    setFont(new Font("Arial", isSelected ? Font.BOLD : Font.PLAIN, 13));
                }
                setBackground(isSelected ? ThemeManager.ACCENT_BLUE : ThemeManager.getBg());
                setForeground(isSelected ? Color.WHITE : ThemeManager.getText());
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
        return combo;
    }

    private JTextArea buildTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 25);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBackground(ThemeManager.getBg());
        ta.setForeground(ThemeManager.getText());
        ta.setCaretColor(ThemeManager.getText());
        ta.setFont(new Font("Arial", Font.PLAIN, 13));
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return ta;
    }

    private JScrollPane styledScrollPane(JTextArea ta) {
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder(), 1));
        sp.getViewport().setBackground(ThemeManager.getBg());
        return sp;
    }

    private int addSectionHeader(JPanel p, int row, String titre, Color color) {
        GridBagConstraints g = gbc(0, row, 2);
        g.insets = new Insets(18, 0, 6, 0);
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JLabel lbl = new JLabel(titre);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(color);
        JPanel line = new JPanel() {
            protected void paintComponent(Graphics g2) {
                Graphics2D g = (Graphics2D) g2.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
                g.dispose();
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(0, 10));
        header.add(lbl,  BorderLayout.WEST);
        header.add(line, BorderLayout.CENTER);
        p.add(header, g);
        return row + 1;
    }

    private int addRow(JPanel p, int row, String label, JComponent field) {
        GridBagConstraints gl = gbc(0, row, 1); gl.weightx = 0.25;
        p.add(fieldLabel(label), gl);
        GridBagConstraints gr = gbc(1, row, 1); gr.weightx = 0.75;
        p.add(field, gr);
        return row + 1;
    }

    private GridBagConstraints gbc(int x, int y, int width) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = width;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 8, 6, 8);
        return g;
    }

    private JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(ThemeManager.getText());
        return l;
    }

    private DocumentListener docListener(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { r.run(); }
            public void removeUpdate(DocumentEvent e)  { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }
}