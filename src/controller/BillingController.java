package controller;

import dao.BillDAO;
import dao.ProductDAO;
import model.Bill;
import model.BillItem;
import model.Product;
import model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controller for POS Billing, cart calculations, stock validation, and checkout.
 * Follows MVC pattern.
 */
public class BillingController {

    private final ProductDAO productDAO;
    private final BillDAO billDAO;
    private Bill currentBill;
    private User cashier;

    public BillingController(User cashier) {
        this.productDAO = new ProductDAO();
        this.billDAO = new BillDAO();
        this.cashier = cashier;
        initNewBill();
    }

    public void initNewBill() {
        String billNo = generateBillNumber();
        Integer cashierId = (cashier != null) ? cashier.getId() : null;
        String cashierName = (cashier != null) ? cashier.getFullName() : "Cashier";
        this.currentBill = new Bill(billNo, cashierId, cashierName);
        this.currentBill.setTaxRate(BigDecimal.valueOf(0.00)); // Default 0% tax, configurable
    }

    private String generateBillNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        return "B" + sdf.format(new Date());
    }

    public Bill getCurrentBill() {
        return currentBill;
    }

    public List<Product> searchProducts(String query) throws SQLException {
        return productDAO.searchProducts(query);
    }

    public Product getProductByBarcode(String barcode) throws SQLException {
        return productDAO.getProductByBarcode(barcode);
    }

    /**
     * Adds an item to the current bill with live stock checking.
     */
    public void addItem(Product product, int quantity) throws Exception {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        // Live check against database stock
        Product fresh = productDAO.getProductById(product.getId());
        if (fresh == null) {
            throw new IllegalArgumentException("Product not found in database.");
        }

        // Calculate quantity already in cart
        int existingInCart = 0;
        for (BillItem item : currentBill.getItems()) {
            if (item.getProductId() != null && item.getProductId().equals(product.getId())) {
                existingInCart = item.getQuantity();
                break;
            }
        }

        int totalRequested = existingInCart + quantity;
        if (totalRequested > fresh.getStock()) {
            throw new IllegalArgumentException("Insufficient stock for '" + fresh.getName() +
                    "'. Available: " + fresh.getStock() + ", in cart: " + existingInCart + ", requested: " + quantity);
        }

        BillItem item = new BillItem(fresh.getId(), fresh.getName(), fresh.getPrice(), quantity);
        currentBill.addItem(item);
    }

    public void updateQuantity(int index, int quantity) throws Exception {
        if (index < 0 || index >= currentBill.getItems().size()) {
            return;
        }

        if (quantity <= 0) {
            currentBill.removeItem(index);
            return;
        }

        BillItem item = currentBill.getItems().get(index);
        if (item.getProductId() != null) {
            Product fresh = productDAO.getProductById(item.getProductId());
            if (fresh != null && quantity > fresh.getStock()) {
                throw new IllegalArgumentException("Insufficient stock for '" + fresh.getName() +
                        "'. Available: " + fresh.getStock() + ", requested: " + quantity);
            }
        }
        currentBill.updateItemQuantity(index, quantity);
    }

    public void removeItem(int index) {
        currentBill.removeItem(index);
    }

    public void clearBill() {
        currentBill.clear();
    }

    public void setTaxRate(BigDecimal rate) {
        currentBill.setTaxRate(rate);
    }

    /**
     * Executes the checkout transaction.
     * Decrements stock in the database and creates the sales record.
     */
    public Bill checkout(String paymentMethod) throws Exception {
        if (currentBill.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart.");
        }

        currentBill.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");
        currentBill.setStatus("COMPLETED");

        try {
            boolean success = billDAO.createBillWithItems(currentBill);
            if (!success) {
                throw new Exception("Transaction could not be completed.");
            }

            // Return snapshot of completed bill
            Bill completed = currentBill;
            initNewBill(); // Reset cart for next customer
            return completed;
        } catch (SQLException e) {
            throw new Exception("Checkout failed: " + e.getMessage(), e);
        }
    }
}
