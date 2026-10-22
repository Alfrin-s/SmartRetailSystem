package dao;

import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product catalog and inventory records.
 */
public class ProductDAO extends BaseDAO {

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name ASC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
            return list;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapProduct(rs);
            }
            return null;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public Product getProductByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM products WHERE barcode = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, barcode.trim());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapProduct(rs);
            }
            return null;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public List<Product> searchProducts(String query) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE barcode LIKE ? OR LOWER(name) LIKE ? OR LOWER(category) LIKE ? ORDER BY name ASC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            String term = "%" + (query != null ? query.trim().toLowerCase() : "") + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);
            stmt.setString(3, term);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
            return list;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock <= min_stock ORDER BY stock ASC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
            return list;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public boolean createProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (barcode, name, category, price, stock, min_stock, unit) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setInt(6, product.getMinStock());
            stmt.setString(7, product.getUnit());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET barcode = ?, name = ?, category = ?, price = ?, stock = ?, min_stock = ?, unit = ?, updated_at = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setInt(6, product.getMinStock());
            stmt.setString(7, product.getUnit());
            stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(9, product.getId());

            return stmt.executeUpdate() > 0;
        } finally {
            close(stmt, conn);
        }
    }

    public boolean updateStock(int productId, int newStock) throws SQLException {
        String sql = "UPDATE products SET stock = ?, updated_at = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newStock);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        } finally {
            close(stmt, conn);
        }
    }

    public boolean deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } finally {
            close(stmt, conn);
        }
    }

    public int getTotalProductCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE stock <= min_stock";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            close(rs, stmt, conn);
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setBarcode(rs.getString("barcode"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setMinStock(rs.getInt("min_stock"));
        p.setUnit(rs.getString("unit"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
