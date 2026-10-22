package view;

import controller.ReportController;
import model.SalesReport;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Sales Reports Screen matching Screen 8 wireframe.
 * Displays periodic reporting, metrics, tabular breakdown, and 2D sales chart.
 */
public class ReportsPanel extends JPanel {

    private final ReportController reportController;

    private JComboBox<String> comboPeriod;
    private JLabel lblTotalSales;
    private JLabel lblTotalBills;
    private JLabel lblTotalItems;
    private DefaultTableModel tableModel;
    private JTable reportTable;
    private SalesBarChartPanel chartPanel;

    public ReportsPanel() {
        this.reportController = new ReportController();
        initComponents();
        loadReport("Daily");
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyleUtil.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Section: Title & Period Selector
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JLabel lblTitle = new JLabel("SALES & REVENUE ANALYTICS");
        lblTitle.setFont(UIStyleUtil.FONT_TITLE);
        lblTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JLabel lblFilter = new JLabel("Reporting Period:");
        lblFilter.setFont(UIStyleUtil.FONT_BOLD);

        comboPeriod = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly"});
        JButton btnGenerate = UIStyleUtil.createPrimaryButton("Generate Report");

        btnGenerate.addActionListener(e -> {
            String selected = (String) comboPeriod.getSelectedItem();
            loadReport(selected);
        });

        filterPanel.add(lblFilter);
        filterPanel.add(comboPeriod);
        filterPanel.add(btnGenerate);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content Container
        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        // 1. KPI Metric Summary Row
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JPanel cardSales = UIStyleUtil.createStatCard("Total Revenue", "\u20B90.00", UIStyleUtil.COLOR_SUCCESS);
        lblTotalSales = extractValueLabel(cardSales);

        JPanel cardBills = UIStyleUtil.createStatCard("Total Bills Processed", "0", UIStyleUtil.COLOR_ACCENT);
        lblTotalBills = extractValueLabel(cardBills);

        JPanel cardItems = UIStyleUtil.createStatCard("Total Items Sold", "0", UIStyleUtil.COLOR_WARNING);
        lblTotalItems = extractValueLabel(cardItems);

        kpiPanel.add(cardSales);
        kpiPanel.add(cardBills);
        kpiPanel.add(cardItems);
        centerContainer.add(kpiPanel, BorderLayout.NORTH);

        // 2. Middle & Bottom Split: Table & Chart
        JPanel dataContainer = new JPanel(new GridLayout(1, 2, 16, 0));
        dataContainer.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        // Left: Sales Table Card
        JPanel tableCard = UIStyleUtil.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel lblTableTitle = new JLabel("Period Sales Breakdown");
        lblTableTitle.setFont(UIStyleUtil.FONT_SUBTITLE);
        lblTableTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Date / Period", "Bills", "Revenue (\u20B9)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        UIStyleUtil.styleTable(reportTable);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyleUtil.COLOR_BORDER));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Right: Chart Card
        JPanel chartCard = UIStyleUtil.createCardPanel();
        chartCard.setLayout(new BorderLayout(0, 10));

        JLabel lblChartTitle = new JLabel("Visual Trend");
        lblChartTitle.setFont(UIStyleUtil.FONT_SUBTITLE);
        lblChartTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        chartCard.add(lblChartTitle, BorderLayout.NORTH);

        chartPanel = new SalesBarChartPanel();
        chartCard.add(chartPanel, BorderLayout.CENTER);

        dataContainer.add(tableCard);
        dataContainer.add(chartCard);

        centerContainer.add(dataContainer, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }

    private JLabel extractValueLabel(JPanel card) {
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

    public void loadReport(String period) {
        try {
            SalesReport report = reportController.getSalesReport(period);

            lblTotalSales.setText(String.format("\u20B9%,.2f", report.getTotalSales()));
            lblTotalBills.setText(String.valueOf(report.getTotalBills()));
            lblTotalItems.setText(String.valueOf(report.getTotalItemsSold()));

            tableModel.setRowCount(0);
            List<SalesReport.SalesEntry> entries = report.getEntries();
            for (SalesReport.SalesEntry e : entries) {
                tableModel.addRow(new Object[]{
                        e.getDatePeriod(),
                        e.getBillCount(),
                        String.format("%.2f", e.getTotalAmount())
                });
            }

            chartPanel.setData(entries, period + " Revenue Trend");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
