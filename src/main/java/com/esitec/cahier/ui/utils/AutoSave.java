package com.esitec.cahier.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AutoSave {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.cahier_texte/";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Timer timer;
    private final Map<String, JTextArea> fields = new HashMap<>();
    private final String formId;
    private JLabel lblIndicator;
    private boolean dirty = false;

    public AutoSave(String formId, int intervalSeconds) {
        this.formId = formId;
        new File(SAVE_DIR).mkdirs();
        timer = new Timer(intervalSeconds * 1000, e -> {
            if (dirty) { save(); dirty = false; }
        });
        timer.start();
    }

    public void register(String key, JTextArea area) {
        fields.put(key, area);
        area.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { dirty = true; showPending(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { dirty = true; showPending(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { dirty = true; showPending(); }
        });
    }

    public JLabel getIndicator() {
        lblIndicator = new JLabel("💾 Auto-save activé");
        lblIndicator.setFont(new Font("Arial", Font.ITALIC, 10));
        lblIndicator.setForeground(Color.GRAY);
        return lblIndicator;
    }

    private void showPending() {
        if (lblIndicator != null) {
            lblIndicator.setText("✏️ Modification non sauvegardée...");
            lblIndicator.setForeground(ThemeManager.ACCENT_ORANGE);
        }
    }

    private void save() {
        try {
            Properties props = new Properties();
            fields.forEach((k, v) -> props.setProperty(k, v.getText()));
            String path = SAVE_DIR + formId + ".draft";
            try (FileOutputStream fos = new FileOutputStream(path)) {
                props.store(fos, "AutoSave " + LocalDateTime.now());
            }
            if (lblIndicator != null) {
                lblIndicator.setText("💾 Sauvegardé à " + LocalDateTime.now().format(FMT));
                lblIndicator.setForeground(ThemeManager.ACCENT_GREEN);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean restore() {
        String path = SAVE_DIR + formId + ".draft";
        File f = new File(path);
        if (!f.exists()) return false;
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(f)) { props.load(fis); }
            fields.forEach((k, v) -> {
                String val = props.getProperty(k, "");
                if (!val.isBlank()) v.setText(val);
            });
            if (lblIndicator != null) {
                lblIndicator.setText("📂 Brouillon restauré !");
                lblIndicator.setForeground(ThemeManager.ACCENT_BLUE);
            }
            return true;
        } catch (IOException e) { return false; }
    }

    public void clear() {
        new File(SAVE_DIR + formId + ".draft").delete();
        if (lblIndicator != null) {
            lblIndicator.setText("💾 Auto-save activé");
            lblIndicator.setForeground(Color.GRAY);
        }
        dirty = false;
    }

    public void stop() { timer.stop(); }
}
