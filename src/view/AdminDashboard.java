package view;

import model.User;
import util.DatabaseConnection;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin Dashboard Frame matching Screen 4 wireframe.
 * Features responsive sidebar navigation, top status banner, and CardLayout content switching.
 */
public class AdminDashboard extends JFrame {

    private final User adminUser;
    private JPanel contentContainer;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Child Views
    private DashboardHomePanel dashboardHomePanel;
    private ProductPanel productPanel;
    private InventoryPanel inventoryPanel;
    private BillingPanel billingPanel;
    private UserPanel userPanel;
    private ReportsPanel reportsPanel;

    public AdminDashboard(User adminUser) {
        super("Smart Retail Inventory & Billing System - Admin Portal");
        this.adminUser = adminUser;
        initComponents();
        setSize(1240, 780);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyleUtil.COLOR_PRIMARY_DARK);
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel brandLabel = new JLabel("Smart Retail POS \u2014 Back Office");
        brandLabel.setFont(UIStyleUtil.FONT_SUBTITLE);
        brandLabel.setForeground(Color.WHITE);

        JPanel userStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        userStatusPanel.setBackground(UIStyleUtil.COLOR_PRIMARY_DARK);

        String dbBadgeText = "[MySQL Database]";
        JLabel lblDbBadge = new JLabel(dbBadgeText);
        lblDbBadge.setFont(UIStyleUtil.FONT_SMALL);
        lblDbBadge.setForeground(UIStyleUtil.COLOR_CREAM);

        String adminName = (adminUser != null) ? adminUser.getFullName() : "Administrator";
        JLabel lblUser = new JLabel("Logged in as: " + adminName + " (ADMIN)");
        lblUser.setFont(UIStyleUtil.FONT_BOLD);
        lblUser.setForeground(UIStyleUtil.COLOR_CREAM);

        JButton btnLogout = UIStyleUtil.createDangerButton("Logout");
        btnLogout.setPreferredSize(new Dimension(90, 30));
        btnLogout.addActionListener(e -> logout());

        userStatusPanel.add(lblDbBadge);
        userStatusPanel.add(lblUser);
        userStatusPanel.add(btnLogout);

        topBar.add(brandLabel, BorderLayout.WEST);
        topBar.add(userStatusPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 2. Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyleUtil.COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        JLabel lblNav = new JLabel("NAVIGATION");
        lblNav.setFont(UIStyleUtil.FONT_SMALL);
        lblNav.setForeground(UIStyleUtil.COLOR_CREAM);
        lblNav.setBorder(new EmptyBorder(0, 8, 12, 0));
        sidebar.add(lblNav);

        String[] navItems = {"Dashboard", "Products", "Inventory", "Billing", "Users", "Reports"};
        String[] navKeys = {"dashboard", "products", "inventory", "billing", "users", "reports"};

        for (int i = 0; i < navItems.length; i++) {
            String key = navKeys[i];
            JButton navBtn = createNavButton(navItems[i], key);
            navButtons.put(key, navBtn);
            sidebar.add(navBtn);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton btnSideLogout = createNavButton("\u21AA Logout", "logout");
        btnSideLogout.addActionListener(e -> logout());
        sidebar.add(btnSideLogout);

        add(sidebar, BorderLayout.WEST);

        // 3. Main Content Area (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        dashboardHomePanel = new DashboardHomePanel();
        productPanel = new ProductPanel();
        inventoryPanel = new InventoryPanel();
        billingPanel = new BillingPanel(adminUser);
        userPanel = new UserPanel();
        reportsPanel = new ReportsPanel();

        contentContainer.add(dashboardHomePanel, "dashboard");
        contentContainer.add(productPanel, "products");
        contentContainer.add(inventoryPanel, "inventory");
        contentContainer.add(billingPanel, "billing");
        contentContainer.add(userPanel, "users");
        contentContainer.add(reportsPanel, "reports");

        add(contentContainer, BorderLayout.CENTER);

        // Highlight first item
        switchPanel("dashboard");
    }

    private JButton createNavButton(String text, String key) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyleUtil.FONT_BOLD);
        btn.setForeground(UIStyleUtil.COLOR_CREAM);
        btn.setBackground(UIStyleUtil.COLOR_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        if (!"logout".equals(key)) {
            btn.addActionListener(e -> switchPanel(key));
        }
        return btn;
    }

    public void switchPanel(String key) {
        cardLayout.show(contentContainer, key);

        // Highlight selected nav button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(key)) {
                entry.getValue().setBackground(UIStyleUtil.COLOR_ACCENT);
                entry.getValue().setForeground(Color.WHITE);
            } else {
                entry.getValue().setBackground(UIStyleUtil.COLOR_SIDEBAR);
                entry.getValue().setForeground(UIStyleUtil.COLOR_CREAM);
            }
        }

        // Trigger refresh on views when switched
        if ("dashboard".equals(key)) {
            dashboardHomePanel.loadDashboardData();
        } else if ("inventory".equals(key)) {
            inventoryPanel.loadInventoryData("");
        } else if ("products".equals(key)) {
            productPanel.loadProducts("");
        } else if ("reports".equals(key)) {
            reportsPanel.loadReport("Daily");
        } else if ("users".equals(key)) {
            userPanel.loadUsers();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of the Admin Portal?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
