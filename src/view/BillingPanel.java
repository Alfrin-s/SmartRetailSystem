package view;

import controller.BillingController;
import model.Bill;
import model.BillItem;
import model.Product;
import model.User;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * POS Billing Screen matching Screen 6 wireframe.
 * Implements two-column layout for real-time product search, cart operations,
 * automatic pricing/tax calculations, and transactional checkout.
 */
public class BillingPanel extends JPanel {

    private final BillingController billingController;
    private final User cashier;

    // Search / Catalog UI
    private JTextField txtSearch;
    private DefaultTableModel catalogModel;
    private JTable catalogTable;
    private List<Product> searchResults;
    private JSpinner spinQuantity;

    // Cart UI
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblSubtotalVal;
    private JLabel lblTaxVal;
    private JLabel lblGrandTotalVal;
    private JComboBox<String> comboPayment;
    private JComboBox<String> comboTax;

    public BillingPanel(User cashier) {
        this.cashier = cashier;
        this.billingController = new BillingController(cashier);
        initComponents();
        performSearch("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyleUtil.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JLabel lblTitle = new JLabel("NEW BILL / POINT OF SALE");
        lblTitle.setFont(UIStyleUtil.FONT_TITLE);
        lblTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);

        String cashierName = (cashier != null) ? cashier.getFullName() : "Cashier";
        JLabel lblCashier = new JLabel("Cashier: " + cashierName + " (" + (cashier != null ? cashier.getRole() : "") + ")");
        lblCashier.setFont(UIStyleUtil.FONT_HEADER);
        lblCashier.setForeground(UIStyleUtil.COLOR_ACCENT);

        topHeader.add(lblTitle, BorderLayout.WEST);
        topHeader.add(lblCashier, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // Center Split (Two Columns)
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContent.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        // ================= COLUMN 1: PRODUCT SEARCH & SELECTION =================
        JPanel leftPanel = UIStyleUtil.createCardPanel();
        leftPanel.setLayout(new BorderLayout(0, 12));

        JLabel lblCatalogTitle = new JLabel("Search & Add Products");
        lblCatalogTitle.setFont(UIStyleUtil.FONT_SUBTITLE);
        lblCatalogTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        leftPanel.add(lblCatalogTitle, BorderLayout.NORTH);

        // Search Input Panel
        JPanel searchBoxPanel = new JPanel(new BorderLayout(8, 0));
        searchBoxPanel.setBackground(Color.WHITE);

        txtSearch = UIStyleUtil.createTextField(15);
        txtSearch.setToolTipText("Type product name or scan barcode");
        JButton btnClearSearch = UIStyleUtil.createSecondaryButton("Clear");
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            performSearch("");
        });

        searchBoxPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchBoxPanel.add(txtSearch, BorderLayout.CENTER);
        searchBoxPanel.add(btnClearSearch, BorderLayout.EAST);

        // Catalog Table
        String[] catCols = {"ID", "Barcode", "Product", "Price (\u20B9)", "Stock"};
        catalogModel = new DefaultTableModel(catCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        catalogTable = new JTable(catalogModel);
        UIStyleUtil.styleTable(catalogTable);

        JScrollPane catalogScroll = new JScrollPane(catalogTable);
        catalogScroll.setBorder(BorderFactory.createLineBorder(UIStyleUtil.COLOR_BORDER));

        // Add to Cart Bar
        JPanel addToCartBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        addToCartBar.setBackground(Color.WHITE);

        JLabel lblQty = new JLabel("Qty:");
        lblQty.setFont(UIStyleUtil.FONT_BOLD);
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spinQuantity.setPreferredSize(new Dimension(65, 32));

        JButton btnAddToCart = UIStyleUtil.createPrimaryButton("+ Add to Bill");
        btnAddToCart.addActionListener(e -> addSelectedToCart());

        addToCartBar.add(lblQty);
        addToCartBar.add(spinQuantity);
        addToCartBar.add(btnAddToCart);

        JPanel leftCenter = new JPanel(new BorderLayout(0, 10));
        leftCenter.setBackground(Color.WHITE);
        leftCenter.add(searchBoxPanel, BorderLayout.NORTH);
        leftCenter.add(catalogScroll, BorderLayout.CENTER);
        leftCenter.add(addToCartBar, BorderLayout.SOUTH);

        leftPanel.add(leftCenter, BorderLayout.CENTER);

        // ================= COLUMN 2: CURRENT BILL & CART =================
        JPanel rightPanel = UIStyleUtil.createCardPanel();
        rightPanel.setLayout(new BorderLayout(0, 12));

        JLabel lblCartTitle = new JLabel("CURRENT BILL");
        lblCartTitle.setFont(UIStyleUtil.FONT_SUBTITLE);
        lblCartTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        rightPanel.add(lblCartTitle, BorderLayout.NORTH);

        // Cart Table
        String[] cartCols = {"Product", "Qty", "Price (\u20B9)", "Total (\u20B9)"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartModel);
        UIStyleUtil.styleTable(cartTable);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(UIStyleUtil.COLOR_BORDER));

        // Cart Item Controls
        JPanel cartItemControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cartItemControls.setBackground(Color.WHITE);

        JButton btnPlus = UIStyleUtil.createSecondaryButton("+ 1");
        JButton btnMinus = UIStyleUtil.createSecondaryButton("- 1");
        JButton btnRemoveItem = UIStyleUtil.createDangerButton("Remove Item");

        btnPlus.addActionListener(e -> adjustSelectedQuantity(1));
        btnMinus.addActionListener(e -> adjustSelectedQuantity(-1));
        btnRemoveItem.addActionListener(e -> removeSelectedItem());

        cartItemControls.add(btnPlus);
        cartItemControls.add(btnMinus);
        cartItemControls.add(btnRemoveItem);

        // Totals & Checkout Summary
        JPanel checkoutSummary = new JPanel();
        checkoutSummary.setLayout(new BoxLayout(checkoutSummary, BoxLayout.Y_AXIS));
        checkoutSummary.setBackground(Color.WHITE);
        checkoutSummary.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyleUtil.COLOR_BORDER),
                new EmptyBorder(12, 0, 0, 0)
        ));

        JPanel rowSubtotal = createSummaryRow("Subtotal:", "\u20B90.00");
        lblSubtotalVal = (JLabel) rowSubtotal.getComponent(1);

        JPanel rowTax = new JPanel(new BorderLayout());
        rowTax.setBackground(Color.WHITE);
        JPanel taxSelector = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        taxSelector.setBackground(Color.WHITE);
        taxSelector.add(new JLabel("Tax Rate:"));
        comboTax = new JComboBox<>(new String[]{"0% GST", "5% GST", "12% GST", "18% GST"});
        comboTax.addActionListener(e -> onTaxRateChanged());
        taxSelector.add(comboTax);

        lblTaxVal = new JLabel("\u20B90.00", SwingConstants.RIGHT);
        lblTaxVal.setFont(UIStyleUtil.FONT_BOLD);
        rowTax.add(taxSelector, BorderLayout.WEST);
        rowTax.add(lblTaxVal, BorderLayout.EAST);

        JPanel rowTotal = createSummaryRow("GRAND TOTAL:", "\u20B90.00");
        JLabel lblGrandTitle = (JLabel) rowTotal.getComponent(0);
        lblGrandTitle.setFont(UIStyleUtil.FONT_HEADER);
        lblGrandTotalVal = (JLabel) rowTotal.getComponent(1);
        lblGrandTotalVal.setFont(UIStyleUtil.FONT_STAT_VALUE);
        lblGrandTotalVal.setForeground(UIStyleUtil.COLOR_ACCENT);

        JPanel rowPayment = new JPanel(new BorderLayout());
        rowPayment.setBackground(Color.WHITE);
        rowPayment.setBorder(new EmptyBorder(4, 0, 8, 0));
        JLabel lblPay = new JLabel("Payment Method:");
        lblPay.setFont(UIStyleUtil.FONT_BOLD);
        comboPayment = new JComboBox<>(new String[]{"CASH", "CREDIT CARD", "DEBIT CARD", "UPI / QR"});
        rowPayment.add(lblPay, BorderLayout.WEST);
        rowPayment.add(comboPayment, BorderLayout.EAST);

        JPanel rowButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        rowButtons.setBackground(Color.WHITE);
        JButton btnClear = UIStyleUtil.createSecondaryButton("CLEAR BILL");
        JButton btnCheckout = UIStyleUtil.createSuccessButton("GENERATE BILL");

        btnClear.addActionListener(e -> onClearCart());
        btnCheckout.addActionListener(e -> onGenerateBill());

        rowButtons.add(btnClear);
        rowButtons.add(btnCheckout);

        checkoutSummary.add(rowSubtotal);
        checkoutSummary.add(Box.createVerticalStrut(4));
        checkoutSummary.add(rowTax);
        checkoutSummary.add(Box.createVerticalStrut(6));
        checkoutSummary.add(rowTotal);
        checkoutSummary.add(Box.createVerticalStrut(6));
        checkoutSummary.add(rowPayment);
        checkoutSummary.add(Box.createVerticalStrut(8));
        checkoutSummary.add(rowButtons);

        JPanel rightCenter = new JPanel(new BorderLayout(0, 8));
        rightCenter.setBackground(Color.WHITE);
        rightCenter.add(cartScroll, BorderLayout.CENTER);
        rightCenter.add(cartItemControls, BorderLayout.SOUTH);

        rightPanel.add(rightCenter, BorderLayout.CENTER);
        rightPanel.add(checkoutSummary, BorderLayout.SOUTH);

        mainContent.add(leftPanel);
        mainContent.add(rightPanel);
        add(mainContent, BorderLayout.CENTER);

        // Search live listener
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { performSearch(txtSearch.getText()); }
            public void removeUpdate(DocumentEvent e) { performSearch(txtSearch.getText()); }
            public void changedUpdate(DocumentEvent e) { performSearch(txtSearch.getText()); }
        });

        // Double click to add to cart
        catalogTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    addSelectedToCart();
                }
            }
        });
    }

    private JPanel createSummaryRow(String label, String initialVal) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setFont(UIStyleUtil.FONT_REGULAR);
        JLabel v = new JLabel(initialVal, SwingConstants.RIGHT);
        v.setFont(UIStyleUtil.FONT_BOLD);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private void performSearch(String query) {
        try {
            catalogModel.setRowCount(0);
            searchResults = billingController.searchProducts(query);
            for (Product p : searchResults) {
                catalogModel.addRow(new Object[]{
                        p.getId(),
                        p.getBarcode(),
                        p.getName(),
                        String.format("%.2f", p.getPrice()),
                        p.getStock()
                });
            }
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    private void addSelectedToCart() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the list.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product p = searchResults.get(selectedRow);
        int qty = (Integer) spinQuantity.getValue();

        try {
            billingController.addItem(p, qty);
            updateCartDisplay();
            spinQuantity.setValue(1); // Reset spinner
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Add Item", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void adjustSelectedQuantity(int delta) {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item in the bill to adjust quantity.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Bill current = billingController.getCurrentBill();
        BillItem item = current.getItems().get(selectedRow);
        int newQty = item.getQuantity() + delta;

        try {
            billingController.updateQuantity(selectedRow, newQty);
            updateCartDisplay();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Stock Limit", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item in the bill to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        billingController.removeItem(selectedRow);
        updateCartDisplay();
    }

    private void onTaxRateChanged() {
        String selected = (String) comboTax.getSelectedItem();
        BigDecimal rate = BigDecimal.ZERO;
        if (selected != null) {
            if (selected.startsWith("5%")) rate = BigDecimal.valueOf(5.0);
            else if (selected.startsWith("12%")) rate = BigDecimal.valueOf(12.0);
            else if (selected.startsWith("18%")) rate = BigDecimal.valueOf(18.0);
        }
        billingController.setTaxRate(rate);
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        Bill bill = billingController.getCurrentBill();
        cartModel.setRowCount(0);

        for (BillItem item : bill.getItems()) {
            cartModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    String.format("%.2f", item.getUnitPrice()),
                    String.format("%.2f", item.getTotalPrice())
            });
        }

        lblSubtotalVal.setText(String.format("\u20B9%.2f", bill.getSubtotal()));
        lblTaxVal.setText(String.format("\u20B9%.2f", bill.getTaxAmount()));
        lblGrandTotalVal.setText(String.format("\u20B9%.2f", bill.getGrandTotal()));
    }

    private void onClearCart() {
        if (billingController.getCurrentBill().getItems().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the current bill?",
                "Clear Bill",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            billingController.clearBill();
            updateCartDisplay();
        }
    }

    private void onGenerateBill() {
        if (billingController.getCurrentBill().getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot generate bill: Cart is empty.", "Empty Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentMethod = (String) comboPayment.getSelectedItem();

        try {
            // Execute ACID checkout transaction
            Bill completedBill = billingController.checkout(paymentMethod);

            // Refresh product catalog to show updated stock
            performSearch(txtSearch.getText());
            updateCartDisplay();

            // Display Receipt Dialog
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            ReceiptDialog dialog = new ReceiptDialog(parentFrame, completedBill);
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Transaction failed: " + ex.getMessage(), "Checkout Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
