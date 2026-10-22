package controller;

import dao.ProductDAO;
import model.Product;
import util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for product catalog management.
 * Follows MVC pattern.
 */
public class ProductController {

    private final ProductDAO productDAO;

    public ProductController() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }

    public List<Product> searchProducts(String query) throws SQLException {
        if (!ValidationUtil.isNotEmpty(query)) {
            return productDAO.getAllProducts();
        }
        return productDAO.searchProducts(query);
    }

    public Product getProductByBarcode(String barcode) throws SQLException {
        if (!ValidationUtil.isNotEmpty(barcode)) return null;
        return productDAO.getProductByBarcode(barcode);
    }

    public boolean saveProduct(Product product) throws Exception {
        validateProduct(product);
        try {
            if (product.getId() == null) {
                // Check duplicate barcode
                Product existing = productDAO.getProductByBarcode(product.getBarcode());
                if (existing != null) {
                    throw new IllegalArgumentException("A product with barcode '" + product.getBarcode() + "' already exists.");
                }
                return productDAO.createProduct(product);
            } else {
                // Check duplicate barcode for other products
                Product existing = productDAO.getProductByBarcode(product.getBarcode());
                if (existing != null && !existing.getId().equals(product.getId())) {
                    throw new IllegalArgumentException("Barcode '" + product.getBarcode() + "' is already in use by another product.");
                }
                return productDAO.updateProduct(product);
            }
        } catch (SQLException e) {
            throw new Exception("Error saving product: " + e.getMessage(), e);
        }
    }

    public boolean deleteProduct(int id) throws Exception {
        try {
            return productDAO.deleteProduct(id);
        } catch (SQLException e) {
            throw new Exception("Error deleting product: " + e.getMessage(), e);
        }
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (!ValidationUtil.isValidBarcode(product.getBarcode())) {
            throw new IllegalArgumentException("Barcode must be alphanumeric and non-empty.");
        }
        if (!ValidationUtil.isNotEmpty(product.getName())) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be greater than or equal to 0.");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (product.getMinStock() < 0) {
            throw new IllegalArgumentException("Minimum stock threshold cannot be negative.");
        }
    }
}
