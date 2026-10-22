import util.DatabaseConnection;
import view.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for Smart Retail Inventory & Billing System.
 * Initializes database connection/tables, sets look and feel, and launches LoginFrame.
 */
public class Main {

    public static void main(String[] args) {
        // Set System Look & Feel for native desktop appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        printBanner();

        // Initialize Database & Seed data
        DatabaseConnection.getInstance().initializeDatabase();

        // Launch Login UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static void printBanner() {
        System.out.println("===============================================================");
        System.out.println("     SMART RETAIL INVENTORY & BILLING SYSTEM (POS)");
        System.out.println("     Architecture: Model-View-Controller (MVC) + DAO");
        System.out.println("     " + DatabaseConnection.getInstance().getDatabaseInfo());
        System.out.println("===============================================================");
        System.out.println("Default Credentials:");
        System.out.println("  - Admin   : username='admin'   password='admin123'");
        System.out.println("  - Cashier : username='cashier' password='cashier123'");
        System.out.println("===============================================================");
    }
}
