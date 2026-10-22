package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DatabaseConnection manages pure MySQL JDBC connectivity.
 * Connects to MySQL using com.mysql.cj.jdbc.Driver and automatically creates
 * the 'smart_retail' database and tables if they do not exist.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Properties config;
    private String host;
    private String port;
    private String database;
    private String dbUser;
    private String dbPassword;
    private String serverUrl;
    private String dbUrl;

    private DatabaseConnection() {
        loadConfiguration();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadConfiguration() {
        config = new Properties();
        File propFile = new File("resources/db.properties");
        if (!propFile.exists()) {
            propFile = new File("SmartRetailSystem/resources/db.properties");
        }
        if (!propFile.exists()) {
            propFile = new File("C:/Users/ALFRIN/.gemini/antigravity/scratch/SmartRetailSystem/resources/db.properties");
        }

        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                config.load(in);
            } catch (Exception e) {
                System.err.println("Warning: Could not load db.properties, using defaults: " + e.getMessage());
            }
        }

        this.host = config.getProperty("db.host", "localhost").trim();
        this.port = config.getProperty("db.port", "3306").trim();
        this.database = config.getProperty("db.database", "smart_retail").trim();
        this.dbUser = config.getProperty("db.user", "root").trim();
        this.dbPassword = config.getProperty("db.password", "admin123").trim();

        // Server connection URL (without database, to allow auto-creating database)
        this.serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        // Full database connection URL
        this.dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    /**
     * Returns an active JDBC connection to the MySQL 'smart_retail' database.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to MySQL database '" + database + "' at " + host + ":" + port +
                    ".\nPlease ensure MySQL is running (e.g. in XAMPP or Windows Services) and check resources/db.properties.\nError: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the MySQL database, tables, and seed data if not present.
     */
    public synchronized void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found: " + e.getMessage());
            return;
        }

        // Step 1: Connect to MySQL server and ensure database exists
        try (Connection conn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + database + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        } catch (SQLException e) {
            System.err.println("Notice: Could not auto-create database (it may already exist or user lacks CREATE permission): " + e.getMessage());
        }

        // Step 2: Connect to the specific database and create tables & seed data
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL DEFAULT 'CASHIER', " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;");

            // 2. Products Table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "barcode VARCHAR(50) NOT NULL UNIQUE, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "category VARCHAR(50) NOT NULL, " +
                    "price DECIMAL(10, 2) NOT NULL, " +
                    "stock INT NOT NULL DEFAULT 0, " +
                    "min_stock INT NOT NULL DEFAULT 10, " +
                    "unit VARCHAR(20) NOT NULL DEFAULT 'unit', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;");

            // 3. Bills Table
            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "bill_number VARCHAR(30) NOT NULL UNIQUE, " +
                    "cashier_id INT, " +
                    "cashier_name VARCHAR(100) NOT NULL, " +
                    "subtotal DECIMAL(10, 2) NOT NULL, " +
                    "tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00, " +
                    "tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00, " +
                    "grand_total DECIMAL(10, 2) NOT NULL, " +
                    "payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH', " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");

            // 4. Bill Items Table
            stmt.execute("CREATE TABLE IF NOT EXISTS bill_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "bill_id INT NOT NULL, " +
                    "product_id INT, " +
                    "product_name VARCHAR(100) NOT NULL, " +
                    "unit_price DECIMAL(10, 2) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "total_price DECIMAL(10, 2) NOT NULL, " +
                    "FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");

            // 5. Seed Users
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO users (username, password, full_name, role, status) VALUES " +
                        "('admin', 'admin123', 'System Administrator', 'ADMIN', 'ACTIVE'), " +
                        "('cashier', 'cashier123', 'Rahul Verma', 'CASHIER', 'ACTIVE');");
            }
            rs.close();

            // 6. Seed Products
            ResultSet rsProd = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rsProd.next() && rsProd.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO products (barcode, name, category, price, stock, min_stock, unit) VALUES " +
                        "('10001', 'Basmati Rice (1kg)', 'Grocery', 120.00, 85, 15, 'kg'), " +
                        "('10002', 'Fresh Cow Milk (1L)', 'Dairy', 32.00, 8, 12, 'packet'), " +
                        "('10003', 'Whole Wheat Bread (400g)', 'Bakery', 40.00, 35, 10, 'loaf'), " +
                        "('10004', 'Refined Sugar (1kg)', 'Grocery', 46.00, 110, 20, 'kg'), " +
                        "('10005', 'Sunflower Cooking Oil (1L)', 'Grocery', 145.00, 22, 10, 'bottle'), " +
                        "('10006', 'Farm Fresh Eggs (12 pcs)', 'Dairy', 84.00, 6, 10, 'pack'), " +
                        "('10007', 'Assam Tea Leaves (500g)', 'Beverages', 190.00, 45, 10, 'box'), " +
                        "('10008', 'Instant Filter Coffee (200g)', 'Beverages', 160.00, 9, 10, 'jar'), " +
                        "('10009', 'Organic Wheat Flour (5kg)', 'Grocery', 240.00, 40, 10, 'bag'), " +
                        "('10010', 'Bathing Soap (4x100g)', 'Personal Care', 115.00, 50, 15, 'pack'), " +
                        "('10011', 'Toor Dal (1kg)', 'Grocery', 165.00, 7, 15, 'kg'), " +
                        "('10012', 'Dark Chocolate Bar (100g)', 'Snacks', 95.00, 60, 12, 'bar');");
            }
            rsProd.close();

            System.out.println("MySQL database initialized successfully on `" + database + "` at " + host + ":" + port);
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {}
            }
        }
    }

    public String getDatabaseInfo() {
        return "Database: MySQL (" + host + ":" + port + "/" + database + ")";
    }
}
