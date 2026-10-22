package test;

import dao.BillDAO;
import dao.ProductDAO;
import dao.ReportDAO;
import dao.UserDAO;
import model.Bill;
import model.BillItem;
import model.Product;
import model.SalesReport;
import model.User;
import util.DatabaseConnection;
import util.ReceiptExporter;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Headless Automated Verification Test Suite.
 * Validates database connectivity, authentication, ACID stock deduction transactions,
 * rollback on insufficient stock, receipt export (PDF & TXT), and reporting.
 */
public class SystemVerificationTest {

    public static void main(String[] args) {
        System.out.println(">>> RUNNING SMART RETAIL SYSTEM HEADLESS VERIFICATION <<<");
        int passed = 0;
        int failed = 0;

        // Test 1: Database Initialization
        try {
            System.out.print("[TEST 1] Initializing Database & Seed Data... ");
            DatabaseConnection.getInstance().initializeDatabase();
            System.out.println("PASSED! (" + DatabaseConnection.getInstance().getDatabaseInfo() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 2: User Authentication & RBAC
        try {
            System.out.print("[TEST 2] Testing User Authentication & RBAC... ");
            UserDAO userDAO = new UserDAO();
            User admin = userDAO.authenticate("admin", "admin123");
            User cashier = userDAO.authenticate("cashier", "cashier123");
            User invalid = userDAO.authenticate("admin", "wrongpassword");

            if (admin == null || !admin.isAdmin()) throw new AssertionError("Admin auth failed");
            if (cashier == null || !cashier.isCashier()) throw new AssertionError("Cashier auth failed");
            if (invalid != null) throw new AssertionError("Invalid password check failed");

            System.out.println("PASSED! Admin: " + admin.getFullName() + ", Cashier: " + cashier.getFullName());
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 3: Product Catalog & Search
        Product rice = null;
        try {
            System.out.print("[TEST 3] Testing Product Catalog & Search... ");
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts();
            if (products.isEmpty()) throw new AssertionError("No products found in catalog");

            List<Product> searchResults = productDAO.searchProducts("Rice");
            if (searchResults.isEmpty()) throw new AssertionError("Search for 'Rice' returned 0 items");

            rice = searchResults.get(0);
            System.out.println("PASSED! Found " + products.size() + " catalog products. Sample: " + rice.getName());
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 4: Low Stock Detection
        try {
            System.out.print("[TEST 4] Testing Low Stock Alert Detection... ");
            ProductDAO productDAO = new ProductDAO();
            List<Product> lowStock = productDAO.getLowStockProducts();
            if (lowStock.isEmpty()) throw new AssertionError("Expected low stock items from seed data");
            System.out.println("PASSED! Found " + lowStock.size() + " items needing restock. Sample: " + lowStock.get(0).getName() + " (Stock: " + lowStock.get(0).getStock() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 5: ACID Transaction Checkout & Live Stock Deduction
        Bill testBill = null;
        try {
            System.out.print("[TEST 5] Testing ACID Transaction Checkout (Stock Deduction)... ");
            ProductDAO productDAO = new ProductDAO();
            BillDAO billDAO = new BillDAO();

            Product freshRice = productDAO.getProductById(rice.getId());
            int initialStock = freshRice.getStock();

            // Create bill with 2 units of Rice
            testBill = new Bill("B-TEST-" + System.currentTimeMillis(), 2, "Rahul Verma");
            testBill.addItem(new BillItem(freshRice.getId(), freshRice.getName(), freshRice.getPrice(), 2));
            testBill.setPaymentMethod("CASH");

            boolean success = billDAO.createBillWithItems(testBill);
            if (!success) throw new AssertionError("createBillWithItems returned false");

            Product afterCheckout = productDAO.getProductById(rice.getId());
            if (afterCheckout.getStock() != initialStock - 2) {
                throw new AssertionError("Stock was not deducted correctly. Expected: " + (initialStock - 2) + ", Actual: " + afterCheckout.getStock());
            }

            System.out.println("PASSED! Bill created. Stock deducted from " + initialStock + " -> " + afterCheckout.getStock());
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 6: Transaction Rollback on Insufficient Stock
        try {
            System.out.print("[TEST 6] Testing ACID Transaction Rollback on Over-Stock Attempt... ");
            ProductDAO productDAO = new ProductDAO();
            BillDAO billDAO = new BillDAO();

            Product freshRice = productDAO.getProductById(rice.getId());
            int stockBeforeFailed = freshRice.getStock();

            Bill oversizedBill = new Bill("B-FAIL-" + System.currentTimeMillis(), 2, "Rahul Verma");
            oversizedBill.addItem(new BillItem(freshRice.getId(), freshRice.getName(), freshRice.getPrice(), 99999));

            boolean rollbackTriggered = false;
            try {
                billDAO.createBillWithItems(oversizedBill);
            } catch (SQLException sqle) {
                rollbackTriggered = true;
            }

            Product stockAfterFailed = productDAO.getProductById(rice.getId());
            if (!rollbackTriggered || stockAfterFailed.getStock() != stockBeforeFailed) {
                throw new AssertionError("Rollback did not protect stock integrity! Before: " + stockBeforeFailed + ", After: " + stockAfterFailed.getStock());
            }

            System.out.println("PASSED! Transaction safely rolled back. Stock preserved at " + stockAfterFailed.getStock());
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 7: Receipt Export (Polymorphism: TXT & PDF)
        try {
            System.out.print("[TEST 7] Testing Receipt Generation (Polymorphism: PDF & TXT)... ");
            File testDir = new File("test_receipts");
            ReceiptExporter txtExp = ReceiptExporter.Factory.getExporter("txt");
            ReceiptExporter pdfExp = ReceiptExporter.Factory.getExporter("pdf");

            File txtFile = txtExp.export(testBill, testDir);
            File pdfFile = pdfExp.export(testBill, testDir);

            if (!txtFile.exists() || txtFile.length() == 0) throw new AssertionError("TXT receipt failed to write");
            if (!pdfFile.exists() || pdfFile.length() == 0) throw new AssertionError("PDF receipt failed to write");

            System.out.println("PASSED! Generated " + txtFile.getName() + " (" + txtFile.length() + " bytes) and " + pdfFile.getName() + " (" + pdfFile.length() + " bytes)");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 8: Sales Reporting
        try {
            System.out.print("[TEST 8] Testing Sales Reporting Analytics... ");
            ReportDAO reportDAO = new ReportDAO();
            SalesReport report = reportDAO.getSalesReport("Daily");
            if (report == null || report.getTotalBills() == 0) {
                throw new AssertionError("Report should reflect at least 1 bill from test");
            }
            System.out.println("PASSED! Total Bills: " + report.getTotalBills() + ", Total Revenue: Rs " + report.getTotalSales() + ", Items Sold: " + report.getTotalItemsSold());
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        System.out.println("\n===============================================================");
        System.out.println("VERIFICATION SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("===============================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
