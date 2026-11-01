package utils;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;


public final class PetPlusTheme {
    private PetPlusTheme() {}

    public static final Color PRIMARY = new Color(250, 48, 115);
    public static final Color PRIMARY_DARK = new Color(224, 25, 91);
    public static final Color TEXT = new Color(31, 45, 70);
    public static final Color MUTED = new Color(89, 111, 140);
    public static final Color BG = new Color(252, 250, 251);
    public static final Color BORDER = new Color(240, 225, 231);
    public static final Color WHITE = Color.WHITE;

    private static final Color DARK_BG = new Color(20, 23, 32);
    private static final Color DARK_CARD = new Color(31, 36, 49);
    private static final Color DARK_FIELD = new Color(40, 46, 61);
    private static final Color DARK_TEXT = new Color(238, 241, 247);
    private static final Color DARK_BORDER = new Color(65, 72, 90);
    private static final Color DARK_SELECTION = new Color(80, 49, 69);

    private static boolean darkMode = false;
    private static boolean windowListenerInstalled = false;
    private static final Map<Component, Original> ORIGINALS = new WeakHashMap<>();
    private static final Map<String, Object> DEFAULTS = new HashMap<>();

    private static final class Original {
        final Color bg;
        final Color fg;
        final Border border;
        final Color selectionBg;
        final Color selectionFg;
        final Color grid;
        Original(Component c) {
            bg = c.getBackground();
            fg = c.getForeground();
            border = c instanceof JComponent ? ((JComponent) c).getBorder() : null;
            if (c instanceof JTable) {
                JTable t = (JTable) c;
                selectionBg = t.getSelectionBackground();
                selectionFg = t.getSelectionForeground();
                grid = t.getGridColor();
            } else {
                selectionBg = selectionFg = grid = null;
            }
        }
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

   
    public static void setDarkMode(boolean dark) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setDarkMode(dark));
            return;
        }
        installWindowListener();
        if (darkMode == dark) return;
        darkMode = dark;
        applyDefaults(dark);
        for (Window w : Window.getWindows()) {
            if (w.isDisplayable()) applyCurrent(w);
        }
    }

    private static void installWindowListener() {
        if (windowListenerInstalled) return;
        windowListenerInstalled = true;
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED && darkMode
                    && event.getSource() instanceof Window) {
                Window w = (Window) event.getSource();
                SwingUtilities.invokeLater(() -> {
                    if (w.isDisplayable()) applyCurrent(w);
                });
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    private static void applyDefaults(boolean dark) {
        String[] keys = {"control", "text", "nimbusBase", "Label.foreground",
                "Panel.background", "Viewport.background", "ScrollPane.background",
                "MenuBar.background", "Menu.background", "Menu.foreground",
                "MenuItem.background", "MenuItem.foreground", "PopupMenu.background",
                "TextField.background", "TextField.foreground",
                "PasswordField.background", "PasswordField.foreground",
                "TextArea.background", "TextArea.foreground",
                "ComboBox.background", "ComboBox.foreground",
                "Table.background", "Table.foreground", "Table.gridColor",
                "TableHeader.background", "TableHeader.foreground",
                "OptionPane.background", "OptionPane.messageForeground",
                "Button.background", "Button.foreground",
                "CheckBox.background", "CheckBox.foreground",
                "RadioButton.background", "RadioButton.foreground",
                "TabbedPane.background", "TabbedPane.foreground",
                "List.background", "List.foreground",
                "ScrollBar.background", "ScrollBar.track", "ScrollBar.thumb",
                "Table.selectionBackground", "Table.selectionForeground"};
        for (String key : keys) {
            if (!DEFAULTS.containsKey(key)) DEFAULTS.put(key, UIManager.get(key));
            if (!dark) {
                UIManager.put(key, DEFAULTS.get(key));
            } else if (key.endsWith("foreground") || key.endsWith("messageForeground")) {
                UIManager.put(key, DARK_TEXT);
            } else if (key.equals("Table.gridColor") || key.equals("ScrollBar.thumb")) {
                UIManager.put(key, DARK_BORDER);
            } else if (key.equals("Table.selectionBackground")) {
                UIManager.put(key, DARK_SELECTION);
            } else if (key.equals("nimbusBase")) {
                UIManager.put(key, DARK_BG);
            } else if (key.contains("Text") || key.contains("ComboBox") || key.contains("PasswordField")) {
                UIManager.put(key, DARK_FIELD);
            } else {
                UIManager.put(key, DARK_CARD);
            }
        }
    }

   
    public static void applyCurrent(Component component) {
        if (component == null) return;
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> applyCurrent(component));
            return;
        }
        updateTree(component, darkMode);
        if (component instanceof JComponent) {
            ((JComponent) component).revalidate();
        }
        component.repaint();
    }

    private static void updateTree(Component c, boolean dark) {
        if (dark) {
            Original saved = ORIGINALS.get(c);
            if (saved == null) {
                saved = new Original(c);
                ORIGINALS.put(c, saved);
            }
            styleDark(c, saved);
        } else {
            Original original = ORIGINALS.remove(c);
            if (original != null) restore(c, original);
        }
        if (c instanceof JMenu) {
            updateTree(((JMenu) c).getPopupMenu(), dark);
        }
        if (c instanceof JTable) {
            JTableHeader h = ((JTable) c).getTableHeader();
            if (h != null) updateTree(h, dark);
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                updateTree(child, dark);
            }
        }
    }

    private static void styleDark(Component c, Original original) {
        Color accent = original.fg;
        boolean keepAccent = accent != null && isAccent(accent);
        if (c instanceof JTable) {
            JTable table = (JTable) c;
            table.setBackground(DARK_CARD);
            table.setForeground(DARK_TEXT);
            table.setSelectionBackground(DARK_SELECTION);
            table.setSelectionForeground(DARK_TEXT);
            table.setGridColor(DARK_BORDER);
        } else if (c instanceof JTableHeader) {
            c.setBackground(DARK_FIELD);
            c.setForeground(DARK_TEXT);
        } else if (c instanceof JTextComponent || c instanceof JComboBox || c instanceof JSpinner) {
            c.setBackground(DARK_FIELD);
            c.setForeground(DARK_TEXT);
            if (c instanceof JTextComponent) {
                ((JTextComponent)c).setCaretColor(DARK_TEXT);
            }
        } else if (c instanceof JMenuItem || c instanceof JPopupMenu || c instanceof JMenu) {
            c.setBackground(DARK_CARD);
            c.setForeground(DARK_TEXT);
        } else if (c instanceof AbstractButton) {
            c.setBackground(original.bg != null && isAccent(original.bg)
                    ? original.bg : DARK_FIELD);
            c.setForeground(original.bg != null && isAccent(original.bg)
                    ? Color.WHITE : DARK_TEXT);
        } else if (c instanceof JLabel) {
            c.setForeground(keepAccent ? accent : DARK_TEXT);
        } else if (c instanceof JViewport) {
            JViewport view = (JViewport)c;
            c.setBackground(view.getView() instanceof JTable ? DARK_CARD : DARK_BG);
        } else if (c instanceof JScrollPane) {
            c.setBackground(DARK_BG);
            c.setForeground(DARK_TEXT);
        } else {
            c.setBackground(paintColor(original.bg));
            c.setForeground(keepAccent ? accent : DARK_TEXT);
        }
        if (c instanceof JComponent && original.border != null) {
            ((JComponent)c).setBorder(darkBorder(original.border));
        }
    }

    private static void restore(Component c, Original original) {
        c.setBackground(original.bg);
        c.setForeground(original.fg);
        if (c instanceof JComponent) ((JComponent)c).setBorder(original.border);
        if (c instanceof JTable) {
            JTable t = (JTable)c;
            t.setSelectionBackground(original.selectionBg);
            t.setSelectionForeground(original.selectionFg);
            t.setGridColor(original.grid);
        }
        if (c instanceof JTextComponent) {
            ((JTextComponent)c).setCaretColor(original.fg);
        }
    }

    private static Border darkBorder(Border border) {
        if (border instanceof LineBorder) {
            LineBorder line = (LineBorder)border;
            return BorderFactory.createLineBorder(DARK_BORDER, line.getThickness(), line.getRoundedCorners());
        }
        if (border instanceof CompoundBorder) {
            CompoundBorder comp = (CompoundBorder)border;
            return BorderFactory.createCompoundBorder(
                    comp.getOutsideBorder() == null ? null : darkBorder(comp.getOutsideBorder()),
                    comp.getInsideBorder() == null ? null : darkBorder(comp.getInsideBorder()));
        }
        return border;
    }

    private static boolean isAccent(Color c) {
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        return (max - min) > 105 && min < 180 && max > 95;
    }

   
    public static Color paintColor(Color lightColor) {
        if (!darkMode || lightColor == null) return lightColor;
        if (isAccent(lightColor)) {
           
            int min = Math.min(lightColor.getRed(), Math.min(lightColor.getGreen(), lightColor.getBlue()));
            if (min < 205) return lightColor;
        }
        int brightness = (lightColor.getRed() + lightColor.getGreen() + lightColor.getBlue()) / 3;
        if (brightness >= 247) return DARK_CARD;
        if (brightness >= 185) return DARK_BG;
        return DARK_FIELD;
    }

   
    public static void apply(Container container) {
        if (container == null) return;
        container.setBackground(BG);
        styleChildren(container);
        if (darkMode) applyCurrent(container);
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
