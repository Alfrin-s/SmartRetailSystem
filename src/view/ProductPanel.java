package view;

import controller.ProductController;
import model.Product;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Product Management Panel for viewing, searching, adding, and editing catalog items.
 */
public class ProductPanel extends JPanel {

    private final ProductController productController;
    private DefaultTableModel tableModel;
    private JTable productTable;
    private JTextField txtSearch;
    private List<Product> currentProductList;

    public ProductPanel() {
        this.productController = new ProductController();
        initComponents();
        loadProducts("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyleUtil.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JLabel lblTitle = new JLabel("PRODUCT CATALOG");
        lblTitle.setFont(UIStyleUtil.FONT_TITLE);
        lblTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Content Card
        JPanel card = UIStyleUtil.createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        // Toolbar: Search + Action Buttons
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIStyleUtil.FONT_BOLD);
        txtSearch = UIStyleUtil.createTextField(20);
        JButton btnSearch = UIStyleUtil.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> loadProducts(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadProducts(txtSearch.getText()));

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setBackground(Color.WHITE);

        JButton btnAdd = UIStyleUtil.createPrimaryButton("+ Add Product");
        JButton btnEdit = UIStyleUtil.createSecondaryButton("Edit");
        JButton btnDelete = UIStyleUtil.createDangerButton("Delete");
        JButton btnRefresh = UIStyleUtil.createSecondaryButton("Refresh");

        btnAdd.addActionListener(e -> showProductDialog(null));
        btnEdit.addActionListener(e -> editSelectedProduct());
        btnDelete.addActionListener(e -> deleteSelectedProduct());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadProducts("");
        });

        actionsPanel.add(btnAdd);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnDelete);
        actionsPanel.add(btnRefresh);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionsPanel, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Barcode", "Product Name", "Category", "Price (\u20B9)", "Stock", "Min Stock", "Unit"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        UIStyleUtil.styleTable(productTable);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyleUtil.COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadProducts(String query) {
        try {
            tableModel.setRowCount(0);
            currentProductList = productController.searchProducts(query);
            for (Product p : currentProductList) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getBarcode(),
                        p.getName(),
                        p.getCategory(),
                        String.format("%.2f", p.getPrice()),
                        p.getStock(),
                        p.getMinStock(),
                        p.getUnit()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Product selected = currentProductList.get(selectedRow);
        showProductDialog(selected);
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Product selected = currentProductList.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + selected.getName() + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productController.deleteProduct(selected.getId());
                loadProducts(txtSearch.getText());
                JOptionPane.showMessageDialog(this, "Product deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showProductDialog(Product existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add New Product" : "Edit Product", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 12, 12));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 24, 10, 24));

        JTextField tfBarcode = UIStyleUtil.createTextField(15);
        JTextField tfName = UIStyleUtil.createTextField(15);
        JTextField tfCategory = UIStyleUtil.createTextField(15);
        JTextField tfPrice = UIStyleUtil.createTextField(15);
        JTextField tfStock = UIStyleUtil.createTextField(15);
        JTextField tfMinStock = UIStyleUtil.createTextField(15);

        if (existing != null) {
            tfBarcode.setText(existing.getBarcode());
            tfName.setText(existing.getName());
            tfCategory.setText(existing.getCategory());
            tfPrice.setText(existing.getPrice().toString());
            tfStock.setText(String.valueOf(existing.getStock()));
            tfMinStock.setText(String.valueOf(existing.getMinStock()));
        }

        formPanel.add(new JLabel("Barcode:"));
        formPanel.add(tfBarcode);
        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(tfName);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(tfCategory);
        formPanel.add(new JLabel("Price (\u20B9):"));
        formPanel.add(tfPrice);
        formPanel.add(new JLabel("Stock Quantity:"));
        formPanel.add(tfStock);
        formPanel.add(new JLabel("Min Stock Alert:"));
        formPanel.add(tfMinStock);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        JButton btnSave = UIStyleUtil.createPrimaryButton("Save Product");
        JButton btnCancel = UIStyleUtil.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String barcode = tfBarcode.getText().trim();
                String name = tfName.getText().trim();
                String category = tfCategory.getText().trim();
                BigDecimal price = new BigDecimal(tfPrice.getText().trim());
                int stock = Integer.parseInt(tfStock.getText().trim());
                int minStock = Integer.parseInt(tfMinStock.getText().trim());

                Product p = (existing == null) ? new Product() : existing;
                p.setBarcode(barcode);
                p.setName(name);
                p.setCategory(category.isEmpty() ? "General" : category);
                p.setPrice(price);
                p.setStock(stock);
                p.setMinStock(minStock);

                productController.saveProduct(p);
                dialog.dispose();
                loadProducts(txtSearch.getText());
                JOptionPane.showMessageDialog(this, "Product saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Validation Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
