package view;

import model.User;
import util.DatabaseConnection;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cashier Dashboard Frame.
 * Dedicated, focused POS checkout terminal for billing staff.
 */
public class CashierDashboard extends JFrame {

    private final User cashierUser;

    public CashierDashboard(User cashierUser) {
        super("Smart Retail POS \u2014 Cashier Checkout Terminal");
        this.cashierUser = cashierUser;
        initComponents();
        setSize(1180, 740);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyleUtil.COLOR_PRIMARY_DARK);
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel brandLabel = new JLabel("Smart Retail POS \u2014 Checkout Terminal");
        brandLabel.setFont(UIStyleUtil.FONT_SUBTITLE);
        brandLabel.setForeground(Color.WHITE);

        JPanel userStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        userStatusPanel.setBackground(UIStyleUtil.COLOR_PRIMARY_DARK);

        String dbBadge = "[MySQL Database]";
        JLabel lblDb = new JLabel(dbBadge);
        lblDb.setFont(UIStyleUtil.FONT_SMALL);
        lblDb.setForeground(UIStyleUtil.COLOR_CREAM);

        String cashierName = (cashierUser != null) ? cashierUser.getFullName() : "Rahul Verma";
        JLabel lblUser = new JLabel("Cashier: " + cashierName);
        lblUser.setFont(UIStyleUtil.FONT_BOLD);
        lblUser.setForeground(UIStyleUtil.COLOR_CREAM);

        JButton btnLogout = UIStyleUtil.createDangerButton("Logout");
        btnLogout.setPreferredSize(new Dimension(85, 30));
        btnLogout.addActionListener(e -> logout());

        userStatusPanel.add(lblDb);
        userStatusPanel.add(lblUser);
        userStatusPanel.add(btnLogout);

        topBar.add(brandLabel, BorderLayout.WEST);
        topBar.add(userStatusPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Main Center: BillingPanel
        BillingPanel billingPanel = new BillingPanel(cashierUser);
        add(billingPanel, BorderLayout.CENTER);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of the Cashier Terminal?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
