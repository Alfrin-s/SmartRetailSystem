package view;

import controller.InventoryController;
import controller.ProductController;
import model.Inventory;
import model.Product;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Inventory Screen matching Screen 5 wireframe.
 * Displays stock health, low stock indicators, and quick restock actions.
 */
public class InventoryPanel extends JPanel {

    private final InventoryController inventoryController;
    private final ProductController productController;

    private DefaultTableModel tableModel;
    private JTable inventoryTable;
    private JTextField txtSearch;
    private List<Product> currentProducts;
    private JLabel lblStockSummary;

    public InventoryPanel() {
        this.inventoryController = new InventoryController();
        this.productController = new ProductController();
        initComponents();
        loadInventoryData("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyleUtil.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JLabel lblTitle = new JLabel("INVENTORY & STOCK STATUS");
        lblTitle.setFont(UIStyleUtil.FONT_TITLE);
        lblTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);

        lblStockSummary = new JLabel("Stock Health: Loading...");
        lblStockSummary.setFont(UIStyleUtil.FONT_BOLD);
        lblStockSummary.setForeground(UIStyleUtil.COLOR_TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblStockSummary, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content Card
        JPanel card = UIStyleUtil.createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIStyleUtil.FONT_BOLD);
        txtSearch = UIStyleUtil.createTextField(22);
        JButton btnSearch = UIStyleUtil.createSecondaryButton("Search");

        btnSearch.addActionListener(e -> loadInventoryData(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadInventoryData(txtSearch.getText()));

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setBackground(Color.WHITE);

        JButton btnRestock = UIStyleUtil.createSuccessButton("+ Restock Selected");
        JButton btnFilterLow = UIStyleUtil.createSecondaryButton("Low Stock Only");
        JButton btnRefresh = UIStyleUtil.createSecondaryButton("Refresh");

        btnRestock.addActionListener(e -> restockSelected());
        btnFilterLow.addActionListener(e -> loadLowStockOnly());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadInventoryData("");
        });

        actionPanel.add(btnRestock);
        actionPanel.add(btnFilterLow);
        actionPanel.add(btnRefresh);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Product", "Barcode", "Price (\u20B9)", "Stock", "Min Stock", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        UIStyleUtil.styleTable(inventoryTable);

        // Apply Polymorphic Custom Renderer to Status Column (index 6)
        inventoryTable.getColumnModel().getColumn(6).setCellRenderer(new StockStatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyleUtil.COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadInventoryData(String query) {
        try {
            tableModel.setRowCount(0);
            currentProducts = productController.searchProducts(query);
            for (Product p : currentProducts) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getBarcode(),
                        String.format("%.2f", p.getPrice()),
                        p.getStock(),
                        p.getMinStock(),
                        p.getStockStatus()
                });
            }

            Inventory inv = inventoryController.getInventorySummary();
            lblStockSummary.setText(String.format("Total: %d products | Low Stock: %d | Out of Stock: %d",
                    inv.getTotalProducts(), inv.getLowStockCount(), inv.getOutOfStockCount()));

            if (inv.getTotalAlertCount() > 0) {
                lblStockSummary.setForeground(UIStyleUtil.COLOR_WARNING);
            } else {
                lblStockSummary.setForeground(UIStyleUtil.COLOR_SUCCESS);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLowStockOnly() {
        try {
            tableModel.setRowCount(0);
            currentProducts = inventoryController.getLowStockProducts();
            for (Product p : currentProducts) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getBarcode(),
                        String.format("%.2f", p.getPrice()),
                        p.getStock(),
                        p.getMinStock(),
                        p.getStockStatus()
                });
            }
            lblStockSummary.setText("Showing " + currentProducts.size() + " items requiring restock");
            lblStockSummary.setForeground(UIStyleUtil.COLOR_WARNING);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restockSelected() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product row to restock.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product p = currentProducts.get(selectedRow);
        String input = JOptionPane.showInputDialog(this,
                "Enter additional quantity to add to '" + p.getName() + "' (Current: " + p.getStock() + "):",
                "Restock Product",
                JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int addUnits = Integer.parseInt(input.trim());
                inventoryController.restockProduct(p.getId(), addUnits);
                loadInventoryData(txtSearch.getText());
                JOptionPane.showMessageDialog(this, "Stock updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Restock failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
