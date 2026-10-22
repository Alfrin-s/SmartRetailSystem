package util;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UIStyleUtil provides centralized styling with the custom designer palette:
 * - #04151F (Rich Obsidian Black / Deep Night Navy)
 * - #183A37 (Deep Pine Forest Teal)
 * - #EFD6AC (Warm Champagne Sand Cream)
 * - #C44900 (Vibrant Rust Amber Orange)
 */
public class UIStyleUtil {

    // Coolors Palette: https://coolors.co/palette/04151f-183a37-efd6ac-c44900
    public static final Color COLOR_OBSIDIAN = new Color(0x04, 0x15, 0x1F); // #04151F
    public static final Color COLOR_PINE = new Color(0x18, 0x3A, 0x37);     // #183A37
    public static final Color COLOR_CREAM = new Color(0xEF, 0xD6, 0xAC);    // #EFD6AC
    public static final Color COLOR_RUST = new Color(0xC4, 0x49, 0x00);     // #C44900

    // Semantic Mapping
    public static final Color COLOR_PRIMARY = COLOR_PINE;                    // Deep Pine #183A37
    public static final Color COLOR_PRIMARY_DARK = COLOR_OBSIDIAN;           // Obsidian #04151F
    public static final Color COLOR_SIDEBAR = COLOR_OBSIDIAN;                // Sidebar #04151F
    public static final Color COLOR_ACCENT = COLOR_RUST;                     // Rust Orange #C44900
    public static final Color COLOR_ACCENT_HOVER = new Color(168, 62, 0);
    public static final Color COLOR_SUCCESS = COLOR_PINE;                    // Deep Pine #183A37
    public static final Color COLOR_WARNING = COLOR_RUST;                    // Rust #C44900
    public static final Color COLOR_DANGER = new Color(185, 28, 28);         // Crimson
    public static final Color COLOR_BACKGROUND = new Color(248, 246, 242);   // Warm Sand Neutral
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(228, 219, 203);       // Warm Cream Border
    public static final Color COLOR_TEXT_PRIMARY = COLOR_OBSIDIAN;           // Obsidian Text
    public static final Color COLOR_TEXT_MUTED = new Color(105, 120, 118);   // Muted Pine Slate

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD, 26);

    private UIStyleUtil() {}

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_RUST, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_PINE, COLOR_CREAM);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, COLOR_DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_PRIMARY_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setSelectionBackground(COLOR_CREAM);
        table.setSelectionForeground(COLOR_OBSIDIAN);
        table.setGridColor(COLOR_BORDER);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COLOR_PINE);
        header.setForeground(COLOR_CREAM);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        // Left accent bar
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(5, 0));
        bar.setBackground(accentColor);
        card.add(bar, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_CARD_BG);

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_SMALL);
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(FONT_STAT_VALUE);
        lblValue.setForeground(COLOR_TEXT_PRIMARY);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(lblValue);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 34));
        tf.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_REGULAR);
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 34));
        pf.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }
}
