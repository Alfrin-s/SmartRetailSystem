package view;

import controller.InventoryController;
import dao.BillDAO;
import dao.ProductDAO;
import model.Bill;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Admin Home Dashboard showing top metric cards and recent transactions.
 * Matches Screen 4 wireframe.
 */
public class DashboardHomePanel extends JPanel {

    private final ProductDAO productDAO;
    private final BillDAO billDAO;
    private final InventoryController inventoryController;

    private JLabel lblProductsValue;
    private JLabel lblLowStockValue;
    private JLabel lblSalesValue;
    private DefaultTableModel recentSalesModel;

    public DashboardHomePanel() {
        this.productDAO = new ProductDAO();
        this.billDAO = new BillDAO();
        this.inventoryController = new InventoryController();
        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyleUtil.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Section: Title & Refresh
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JLabel titleLabel = new JLabel("DASHBOARD OVERVIEW");
        titleLabel.setFont(UIStyleUtil.FONT_TITLE);
        titleLabel.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);

        JButton btnRefresh = UIStyleUtil.createSecondaryButton("Refresh Data");
        btnRefresh.addActionListener(e -> loadDashboardData());

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);

        // Center Wrapper
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 20));
        centerWrapper.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        // 1. KPI Cards Row
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JPanel card1 = UIStyleUtil.createStatCard("Total Products", "0", UIStyleUtil.COLOR_ACCENT);
        lblProductsValue = getStatValueLabel(card1);

        JPanel card2 = UIStyleUtil.createStatCard("Low Stock Alerts", "0", UIStyleUtil.COLOR_WARNING);
        lblLowStockValue = getStatValueLabel(card2);

        JPanel card3 = UIStyleUtil.createStatCard("Total Revenue", "\u20B90.00", UIStyleUtil.COLOR_SUCCESS);
        lblSalesValue = getStatValueLabel(card3);

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);

        centerWrapper.add(cardsPanel, BorderLayout.NORTH);

        // 2. Recent Sales Section
        JPanel recentSalesPanel = UIStyleUtil.createCardPanel();
        recentSalesPanel.setLayout(new BorderLayout(0, 12));

        JLabel lblRecent = new JLabel("Recent Sales Transactions");
        lblRecent.setFont(UIStyleUtil.FONT_SUBTITLE);
        lblRecent.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        recentSalesPanel.add(lblRecent, BorderLayout.NORTH);

        String[] cols = {"Bill ID", "Date & Time", "Cashier", "Payment Method", "Amount"};
        recentSalesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable salesTable = new JTable(recentSalesModel);
        UIStyleUtil.styleTable(salesTable);

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyleUtil.COLOR_BORDER));
        recentSalesPanel.add(scrollPane, BorderLayout.CENTER);

        centerWrapper.add(recentSalesPanel, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JLabel getStatValueLabel(JPanel card) {
        BorderLayout layout = (BorderLayout) card.getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JPanel) {
            JPanel cp = (JPanel) center;
            for (Component c : cp.getComponents()) {
                if (c instanceof JLabel && ((JLabel) c).getFont().getSize() > 20) {
                    return (JLabel) c;
                }
            }
        }
        return new JLabel();
    }

    public void loadDashboardData() {
        try {
            int prodCount = productDAO.getTotalProductCount();
            int lowStock = productDAO.getLowStockCount();
            BigDecimal totalSales = billDAO.getTotalSalesRevenue();

            lblProductsValue.setText(String.valueOf(prodCount));
            lblLowStockValue.setText(String.valueOf(lowStock));
            lblSalesValue.setText(String.format("\u20B9%,.2f", totalSales));

            // Load recent bills
            recentSalesModel.setRowCount(0);
            List<Bill> recent = billDAO.getRecentBills(10);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            for (Bill b : recent) {
                String dateStr = (b.getCreatedAt() != null) ? sdf.format(b.getCreatedAt()) : "-";
                recentSalesModel.addRow(new Object[]{
                        b.getBillNumber(),
                        dateStr,
                        b.getCashierName(),
                        b.getPaymentMethod(),
                        String.format("\u20B9%.2f", b.getGrandTotal())
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }
}
