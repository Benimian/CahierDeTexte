package com.esitec.cahier.ui.utils;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SearchableTable extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final DefaultTableModel filteredModel;
    private final List<Object[]> allData = new ArrayList<>();
    private final int pageSize;
    private int currentPage = 0;

    private JLabel lblPagination;
    private JButton btnPrev, btnNext;
    private JTextField searchField;

    public SearchableTable(String[] columns, int pageSize) {
        this.pageSize = pageSize;
        setLayout(new BorderLayout(0, 5));
        setOpaque(false);

        filteredModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        model = new DefaultTableModel(columns, 0);
        table = new JTable(filteredModel);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(30, 60, 114, 80));
        styleTable();

        // ── Barre de recherche ────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 ");
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filtrer(searchField.getText()); }
        });

        topBar.add(lblSearch, BorderLayout.WEST);
        topBar.add(searchField, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder()));
        add(scroll, BorderLayout.CENTER);

        // ── Pagination ────────────────────────────────────────────────
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        paginationPanel.setOpaque(false);

        btnPrev = new JButton("◀ Précédent");
        btnNext = new JButton("Suivant ▶");
        lblPagination = new JLabel("Page 1/1");
        lblPagination.setFont(new Font("Arial", Font.BOLD, 12));

        styleNavButton(btnPrev);
        styleNavButton(btnNext);

        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; afficherPage(); } });
        btnNext.addActionListener(e -> { if ((currentPage + 1) * pageSize < allData.size()) { currentPage++; afficherPage(); } });

        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPagination);
        paginationPanel.add(btnNext);
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private void styleNavButton(JButton btn) {
        btn.setBackground(ThemeManager.ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable() {
        JTableHeader header = table.getTableHeader();
        header.setBackground(ThemeManager.getTableHeader());
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 35));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? ThemeManager.getCard() : ThemeManager.getTableAlt());
                    comp.setForeground(ThemeManager.getText());
                }
                ((JLabel) comp).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return comp;
            }
        });
    }

    public void addRow(Object[] row) {
        allData.add(row);
        afficherPage();
    }

    public void clearRows() {
        allData.clear();
        filteredModel.setRowCount(0);
        currentPage = 0;
        updatePagination(0);
    }

    private void filtrer(String query) {
        currentPage = 0;
        if (query.isEmpty()) {
            afficherPage();
            return;
        }
        filteredModel.setRowCount(0);
        for (Object[] row : allData) {
            for (Object cell : row) {
                if (cell != null && cell.toString().toLowerCase().contains(query.toLowerCase())) {
                    filteredModel.addRow(row);
                    break;
                }
            }
        }
        updatePagination(filteredModel.getRowCount());
    }

    private void afficherPage() {
        filteredModel.setRowCount(0);
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        for (int i = start; i < end; i++) {
            filteredModel.addRow(allData.get(i));
        }
        updatePagination(allData.size());
    }

    private void updatePagination(int total) {
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        lblPagination.setText("Page " + (currentPage + 1) + "/" + totalPages);
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled((currentPage + 1) * pageSize < total);
    }

    public JTable getTable() { return table; }

    public int getSelectedRow() { return table.getSelectedRow(); }

    public Object getValueAt(int row, int col) { return filteredModel.getValueAt(row, col); }

    public void refreshTheme() {
        styleTable();
        repaint();
    }
}
