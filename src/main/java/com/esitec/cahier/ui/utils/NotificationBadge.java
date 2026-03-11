package com.esitec.cahier.ui.utils;

import javax.swing.*;
import java.awt.*;

public class NotificationBadge extends JPanel {

    private final String tabTitle;
    private int count = 0;

    public NotificationBadge(String tabTitle) {
        this.tabTitle = tabTitle;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setPreferredSize(new Dimension(160, 26));
        refresh();
    }

    public void setCount(int count) {
        this.count = count;
        refresh();
        repaint();
    }

    private void refresh() {
        removeAll();
        JLabel lblTitle = new JLabel(tabTitle);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblTitle);

        if (count > 0) {
            JLabel badge = new JLabel(String.valueOf(count)) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ThemeManager.ACCENT_RED);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Arial", Font.BOLD, 10));
            badge.setForeground(Color.WHITE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            int size = count > 9 ? 22 : 18;
            badge.setPreferredSize(new Dimension(size, 18));
            add(badge);
        }
        revalidate();
    }
}
