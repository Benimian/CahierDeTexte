package com.esitec.cahier.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConfirmDialog extends JDialog {

    private boolean confirmed = false;

    public ConfirmDialog(JFrame parent, String titre, String message, String iconText) {
        super(parent, titre, true);
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);
        initUI(message, iconText);
    }

    private void initUI(String message, String iconText) {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(ThemeManager.getCard());
        main.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.ACCENT_RED, 2, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // Icône + message
        JPanel top = new JPanel(new BorderLayout(15, 0));
        top.setOpaque(false);

        JLabel icon = new JLabel(iconText);
        icon.setFont(new Font("Arial", Font.PLAIN, 36));
        top.add(icon, BorderLayout.WEST);

        JLabel msg = new JLabel("<html><b>" + message + "</b><br><small>Cette action est irréversible.</small></html>");
        msg.setFont(new Font("Arial", Font.PLAIN, 13));
        msg.setForeground(ThemeManager.getText());
        top.add(msg, BorderLayout.CENTER);

        main.add(top, BorderLayout.CENTER);

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setBackground(new Color(189, 195, 199));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setBorderPainted(false);
        btnAnnuler.setFont(new Font("Arial", Font.BOLD, 12));
        btnAnnuler.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAnnuler.addActionListener(e -> dispose());

        JButton btnConfirmer = new JButton("Confirmer");
        btnConfirmer.setBackground(ThemeManager.ACCENT_RED);
        btnConfirmer.setForeground(Color.WHITE);
        btnConfirmer.setBorderPainted(false);
        btnConfirmer.setFont(new Font("Arial", Font.BOLD, 12));
        btnConfirmer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmer.addActionListener(e -> { confirmed = true; dispose(); });

        btnPanel.add(btnAnnuler);
        btnPanel.add(btnConfirmer);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    public boolean isConfirmed() { return confirmed; }

    // Méthode statique utilitaire
    public static boolean show(JFrame parent, String message) {
        ConfirmDialog d = new ConfirmDialog(parent, "Confirmation", message, "⚠️");
        d.setVisible(true);
        return d.isConfirmed();
    }
}
