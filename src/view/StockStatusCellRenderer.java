package view;

import model.Product;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom TableCellRenderer styled with the new palette.
 * Badges use Pine (#183A37) for OK, Rust (#C44900) for Low Stock, and Cream (#EFD6AC) for selections.
 */
public class StockStatusCellRenderer extends DefaultTableCellRenderer {

    public StockStatusCellRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setFont(UIStyleUtil.FONT_BOLD);
        label.setOpaque(true);

        String status = (value != null) ? value.toString() : "";

        if (Product.STATUS_OUT_OF_STOCK.equalsIgnoreCase(status)) {
            label.setBackground(isSelected ? new Color(254, 202, 202) : new Color(254, 226, 226));
            label.setForeground(new Color(185, 28, 28));
            label.setText(" OUT OF STOCK ");
        } else if (Product.STATUS_LOW.equalsIgnoreCase(status)) {
            label.setBackground(isSelected ? UIStyleUtil.COLOR_CREAM : new Color(253, 244, 231));
            label.setForeground(UIStyleUtil.COLOR_RUST);
            label.setText(" LOW STOCK ");
        } else if (Product.STATUS_OK.equalsIgnoreCase(status)) {
            label.setBackground(isSelected ? new Color(208, 225, 222) : new Color(231, 239, 234));
            label.setForeground(UIStyleUtil.COLOR_PINE);
            label.setText(" OK ");
        } else {
            if (isSelected) {
                label.setBackground(UIStyleUtil.COLOR_CREAM);
                label.setForeground(UIStyleUtil.COLOR_OBSIDIAN);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(253, 251, 247));
                label.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
            }
        }

        return label;
    }
}
