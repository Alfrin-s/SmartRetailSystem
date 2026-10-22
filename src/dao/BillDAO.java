package dao;

import model.Bill;
import model.BillItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Bills and Bill Items.
 * Demonstrates ACID Transactions with commit and rollback for checkout safety.
 */
public class BillDAO extends BaseDAO {

    /**
     * Executes an ACID transaction to create a bill, persist line items,
     * validate stock levels, and deduct inventory atomically.
     */
    public boolean createBillWithItems(Bill bill) throws SQLException {
        if (bill == null || bill.getItems() == null || bill.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create an empty bill.");
        }

        Connection conn = null;
        PreparedStatement billStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockCheckStmt = null;
        PreparedStatement stockDeductStmt = null;
        ResultSet generatedKeys = null;

        String billSql = "INSERT INTO bills (bill_number, cashier_id, cashier_name, subtotal, tax_rate, tax_amount, grand_total, payment_method, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO bill_items (bill_id, product_id, product_name, unit_price, quantity, total_price) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String stockCheckSql = "SELECT stock, name FROM products WHERE id = ?";
        String stockDeductSql = "UPDATE products SET stock = stock - ?, updated_at = ? WHERE id = ?";

        boolean originalAutoCommit = true;

        try {
            conn = getConnection();
            originalAutoCommit = conn.getAutoCommit();
            // Start transaction
            conn.setAutoCommit(false);

            stockCheckStmt = conn.prepareStatement(stockCheckSql);
            stockDeductStmt = conn.prepareStatement(stockDeductSql);

            // Phase 1: Validate stock for all items BEFORE making any changes
            for (BillItem item : bill.getItems()) {
                if (item.getProductId() != null) {
                    stockCheckStmt.setInt(1, item.getProductId());
                    try (ResultSet rs = stockCheckStmt.executeQuery()) {
                        if (rs.next()) {
                            int currentStock = rs.getInt("stock");
                            String prodName = rs.getString("name");
                            if (currentStock < item.getQuantity()) {
                                throw new SQLException("Insufficient stock for product '" + prodName +
                                        "'. Available: " + currentStock + ", Requested: " + item.getQuantity());
                            }
                        } else {
                            throw new SQLException("Product with ID " + item.getProductId() + " not found in catalog.");
                        }
                    }
                }
            }

            // Phase 2: Insert Bill Master Record
            Timestamp now = new Timestamp(System.currentTimeMillis());
            bill.setCreatedAt(now);

            billStmt = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS);
            billStmt.setString(1, bill.getBillNumber());
            if (bill.getCashierId() != null) {
                billStmt.setInt(2, bill.getCashierId());
            } else {
                billStmt.setNull(2, java.sql.Types.INTEGER);
            }
            billStmt.setString(3, bill.getCashierName());
            billStmt.setBigDecimal(4, bill.getSubtotal());
            billStmt.setBigDecimal(5, bill.getTaxRate());
            billStmt.setBigDecimal(6, bill.getTaxAmount());
            billStmt.setBigDecimal(7, bill.getGrandTotal());
            billStmt.setString(8, bill.getPaymentMethod());
            billStmt.setString(9, bill.getStatus());
            billStmt.setTimestamp(10, now);

            int billRows = billStmt.executeUpdate();
            if (billRows == 0) {
                throw new SQLException("Failed to insert bill record, no rows affected.");
            }

            generatedKeys = billStmt.getGeneratedKeys();
            int billId;
            if (generatedKeys.next()) {
                billId = generatedKeys.getInt(1);
                bill.setId(billId);
            } else {
                throw new SQLException("Creating bill failed, no ID obtained.");
            }

            // Phase 3: Insert line items and deduct inventory stock
            itemStmt = conn.prepareStatement(itemSql);
            for (BillItem item : bill.getItems()) {
                item.setBillId(billId);
                itemStmt.setInt(1, billId);
                if (item.getProductId() != null) {
                    itemStmt.setInt(2, item.getProductId());
                } else {
                    itemStmt.setNull(2, java.sql.Types.INTEGER);
                }
                itemStmt.setString(3, item.getProductName());
                itemStmt.setBigDecimal(4, item.getUnitPrice());
                itemStmt.setInt(5, item.getQuantity());
                itemStmt.setBigDecimal(6, item.getTotalPrice());
                itemStmt.addBatch();

                // Deduct stock
                if (item.getProductId() != null) {
                    stockDeductStmt.setInt(1, item.getQuantity());
                    stockDeductStmt.setTimestamp(2, now);
                    stockDeductStmt.setInt(3, item.getProductId());
                    stockDeductStmt.addBatch();
                }
            }

            itemStmt.executeBatch();
            stockDeductStmt.executeBatch();

            // All operations succeeded - Commit transaction!
            conn.commit();
            return true;

        } catch (SQLException e) {
            // Failure occurred - Rollback transaction!
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {}
            }
            close(generatedKeys, billStmt, itemStmt, stockCheckStmt, stockDeductStmt, conn);
        }
    }

    public List<Bill> getRecentBills(int limit) throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY created_at DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapBill(rs));
            }
            return list;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public Bill getBillByNumber(String billNumber) throws SQLException {
        String sql = "SELECT * FROM bills WHERE bill_number = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, billNumber);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Bill bill = mapBill(rs);
                bill.setItems(getBillItems(bill.getId()));
                return bill;
            }
            return null;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public List<BillItem> getBillItems(int billId) throws SQLException {
        List<BillItem> list = new ArrayList<>();
        String sql = "SELECT * FROM bill_items WHERE bill_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, billId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                BillItem item = new BillItem();
                item.setId(rs.getInt("id"));
                item.setBillId(rs.getInt("bill_id"));
                int prodId = rs.getInt("product_id");
                if (!rs.wasNull()) {
                    item.setProductId(prodId);
                }
                item.setProductName(rs.getString("product_name"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setTotalPrice(rs.getBigDecimal("total_price"));
                list.add(item);
            }
            return list;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public BigDecimal getTotalSalesRevenue() throws SQLException {
        String sql = "SELECT SUM(grand_total) FROM bills WHERE status = 'COMPLETED'";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal(1);
                return (sum != null) ? sum : BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        } finally {
            close(rs, stmt, conn);
        }
    }

    public int getTotalBillCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills";
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

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setBillNumber(rs.getString("bill_number"));
        int cId = rs.getInt("cashier_id");
        if (!rs.wasNull()) {
            b.setCashierId(cId);
        }
        b.setCashierName(rs.getString("cashier_name"));
        b.setSubtotal(rs.getBigDecimal("subtotal"));
        b.setTaxRate(rs.getBigDecimal("tax_rate"));
        b.setTaxAmount(rs.getBigDecimal("tax_amount"));
        b.setGrandTotal(rs.getBigDecimal("grand_total"));
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setStatus(rs.getString("status"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}
