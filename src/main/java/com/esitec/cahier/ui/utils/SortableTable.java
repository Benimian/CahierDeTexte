package com.esitec.cahier.ui.utils;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SortableTable extends JTable {

    public interface RowAction {
        String label();
        void execute(int modelRow);
    }

    private final List<RowAction> contextActions = new ArrayList<>();

    public SortableTable(DefaultTableModel model) {
        super(model);
        setRowSorter(new TableRowSorter<>(model));
        applyStyle();
        addContextMenu();
    }

    private void applyStyle() {
        setBackground(ThemeManager.getCard());
        setForeground(ThemeManager.getText());
        setGridColor(ThemeManager.getBorder());
        setRowHeight(36);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(new Color(30, 60, 114, 60));
        setSelectionForeground(ThemeManager.getText());
        setFillsViewportHeight(true);
        setFont(new Font("Arial", Font.PLAIN, 13));

        // En-tête stylisé avec indicateur de tri
        JTableHeader header = getTableHeader();
        header.setBackground(ThemeManager.getTableHeader());
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setBackground(ThemeManager.getTableHeader());
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Arial", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                lbl.setOpaque(true);
                // Indicateur de tri
                if (getRowSorter() != null) {
                    List<? extends RowSorter.SortKey> keys = getRowSorter().getSortKeys();
                    if (!keys.isEmpty() && keys.get(0).getColumn() == c) {
                        String arrow = keys.get(0).getSortOrder() == SortOrder.ASCENDING ? " ▲" : " ▼";
                        lbl.setText(v + arrow);
                    }
                }
                return lbl;
            }
        });

        // Renderer lignes alternées + hover
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component cell = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    cell.setBackground(r % 2 == 0 ? ThemeManager.getCard() : ThemeManager.getTableAlt());
                    cell.setForeground(ThemeManager.getText());
                }
                ((JLabel) cell).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                ((JLabel) cell).setFont(new Font("Arial", Font.PLAIN, 13));
                return cell;
            }
        });

        // Highlight au survol
        addMouseMotionListener(new MouseMotionAdapter() {
            int lastRow = -1;
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != lastRow) {
                    lastRow = row;
                    repaint();
                }
            }
        });
    }

    private void addContextMenu() {
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && !contextActions.isEmpty()) {
                    int row = rowAtPoint(e.getPoint());
                    if (row < 0) return;
                    setRowSelectionInterval(row, row);
                    int modelRow = convertRowIndexToModel(row);
                    showContextMenu(e.getComponent(), e.getX(), e.getY(), modelRow);
                }
            }
        });
    }

    private void showContextMenu(Component comp, int x, int y, int modelRow) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(ThemeManager.getCard());
        menu.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder()));

        for (RowAction action : contextActions) {
            JMenuItem item = new JMenuItem(action.label());
            item.setBackground(ThemeManager.getCard());
            item.setForeground(ThemeManager.getText());
            item.setFont(new Font("Arial", Font.PLAIN, 13));
            item.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            item.addActionListener(e -> action.execute(modelRow));
            menu.add(item);
        }
        menu.show(comp, x, y);
    }

    /** Ajoute une action dans le menu clic droit */
    public void addContextAction(String label, Consumer<Integer> action) {
        contextActions.add(new RowAction() {
            public String label()              { return label; }
            public void execute(int modelRow)  { action.accept(modelRow); }
        });
    }

    /** Barre de recherche intégrée */
    public JTextField buildSearchBar(String placeholder) {
        JTextField search = new JTextField();
        search.setBackground(ThemeManager.getBg());
        search.setForeground(ThemeManager.getText());
        search.setCaretColor(ThemeManager.getText());
        search.setFont(new Font("Arial", Font.PLAIN, 13));
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder()),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        search.putClientProperty("JTextField.placeholderText", placeholder);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            void filter() {
                String text = search.getText().trim();
                TableRowSorter<?> sorter = (TableRowSorter<?>) getRowSorter();
                if (text.isEmpty()) { sorter.setRowFilter(null); }
                else { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); }
            }
        });
        return search;
    }
}
