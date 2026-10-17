package utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.table.JTableHeader;

public final class PetPlusTheme {
    private PetPlusTheme() {}

    public static final Color PRIMARY = new Color(250, 48, 115);
    public static final Color PRIMARY_DARK = new Color(224, 25, 91);
    public static final Color TEXT = new Color(31, 45, 70);
    public static final Color MUTED = new Color(89, 111, 140);
    public static final Color BG = new Color(252, 250, 251);
    public static final Color BORDER = new Color(240, 225, 231);
    public static final Color WHITE = Color.WHITE;

    // Ap dung mau cho container va cac component con da duoc tao. 
    public static void apply(Container container) {
        if (container == null) return;
        container.setBackground(BG);
        styleChildren(container);
        container.revalidate();
        container.repaint();
    }

    private static void styleChildren(Container parent) {
        for (Component component : parent.getComponents()) {
            if (component instanceof JTable) {
                JTable table = (JTable) component;
                table.setBackground(WHITE);
                table.setForeground(TEXT);
                table.setSelectionBackground(new Color(255, 220, 232));
                table.setSelectionForeground(TEXT);
                table.setGridColor(BORDER);
                table.setRowHeight(Math.max(table.getRowHeight(), 26));
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    header.setBackground(PRIMARY);
                    header.setForeground(WHITE);
                    header.setFont(header.getFont().deriveFont(Font.BOLD));
                }
            } else if (component instanceof JRadioButton) {
                JRadioButton option = (JRadioButton) component;
                option.setBackground(BG);
                option.setForeground(TEXT);
                option.setOpaque(false);
                option.setBorder(BorderFactory.createEmptyBorder());
            } else if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBackground(PRIMARY);
                button.setForeground(WHITE);
                button.setFocusPainted(false);
                button.setOpaque(true);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_DARK),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)));
                button.setFont(button.getFont().deriveFont(Font.BOLD));
            } else if (component instanceof JTextField || component instanceof JPasswordField
                    || component instanceof JTextArea || component instanceof JComboBox
                    || component instanceof JSpinner) {
                component.setBackground(WHITE);
                component.setForeground(TEXT);
                if (component instanceof JComponent) {
                    ((JComponent) component).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6)));
                }
            } else if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setForeground(label.getFont().getSize() >= 20 ? PRIMARY_DARK : TEXT);
            } else if (component instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) component;
                scroll.getViewport().setBackground(WHITE);
                scroll.setBorder(BorderFactory.createLineBorder(BORDER));
            } else {
                component.setBackground(BG);
                if (component instanceof Container) styleChildren((Container) component);
            }
        }
    }
}
