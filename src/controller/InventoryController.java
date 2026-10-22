package controller;

import dao.ProductDAO;
import model.Inventory;
import model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for inventory tracking, low-stock alerts, and restocking.
 * Follows MVC pattern.
 */
public class InventoryController {

    private final ProductDAO productDAO;

    public InventoryController() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }

    public List<Product> getLowStockProducts() throws SQLException {
        return productDAO.getLowStockProducts();
    }

    public Inventory getInventorySummary() throws SQLException {
        List<Product> products = productDAO.getAllProducts();
        return Inventory.calculate(products);
    }

    public boolean restockProduct(int productId, int additionalUnits) throws Exception {
        if (additionalUnits <= 0) {
            throw new IllegalArgumentException("Restock units must be greater than 0.");
        }
        Product product = productDAO.getProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found.");
        }
        int newStock = product.getStock() + additionalUnits;
        return productDAO.updateStock(productId, newStock);
    }
}
