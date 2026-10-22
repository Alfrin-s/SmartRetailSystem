package view;

import controller.LoginController;
import model.User;
import util.DatabaseConnection;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Login Screen matching Screen 1 wireframe.
 * Handles user authentication and redirects to AdminDashboard or CashierDashboard based on RBAC role.
 */
public class LoginFrame extends JFrame {

    private final LoginController loginController;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblErrorMessage;
    private JButton btnLogin;

    public LoginFrame() {
        super("Smart Retail Inventory & Billing System - Login");
        this.loginController = new LoginController();
        initComponents();
        setSize(480, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        // Login Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UIStyleUtil.COLOR_BORDER, 1, true),
                new EmptyBorder(32, 36, 32, 36)
        ));
        card.setPreferredSize(new Dimension(380, 420));

        // Title
        JLabel lblTitle = new JLabel("SMART RETAIL SYSTEM");
        lblTitle.setFont(UIStyleUtil.FONT_TITLE);
        lblTitle.setForeground(UIStyleUtil.COLOR_PRIMARY_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Sign in to your account");
        lblSubtitle.setFont(UIStyleUtil.FONT_REGULAR);
        lblSubtitle.setForeground(UIStyleUtil.COLOR_TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(24));

        // Username Field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UIStyleUtil.FONT_BOLD);
        lblUser.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = UIStyleUtil.createTextField(15);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(14));

        // Password Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UIStyleUtil.FONT_BOLD);
        lblPass.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = UIStyleUtil.createPasswordField(15);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(12));

        // Error message
        lblErrorMessage = new JLabel(" ");
        lblErrorMessage.setFont(UIStyleUtil.FONT_REGULAR);
        lblErrorMessage.setForeground(UIStyleUtil.COLOR_DANGER);
        lblErrorMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblErrorMessage);
        card.add(Box.createVerticalStrut(10));

        // Login Button
        btnLogin = UIStyleUtil.createPrimaryButton("LOGIN");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());

        // Enter key listeners
        txtUsername.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());

        card.add(btnLogin);
        card.add(Box.createVerticalStrut(18));

        // Help note showing default logins
        JPanel hintPanel = new JPanel();
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));
        hintPanel.setBackground(new Color(253, 248, 238));
        hintPanel.setBorder(new CompoundBorder(
                new LineBorder(UIStyleUtil.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        hintPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        hintPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblHint1 = new JLabel("Default Admin: admin / admin123");
        lblHint1.setFont(UIStyleUtil.FONT_SMALL);
        lblHint1.setForeground(UIStyleUtil.COLOR_PINE);

        JLabel lblHint2 = new JLabel("Default Cashier: cashier / cashier123");
        lblHint2.setFont(UIStyleUtil.FONT_SMALL);
        lblHint2.setForeground(UIStyleUtil.COLOR_PINE);

        hintPanel.add(lblHint1);
        hintPanel.add(lblHint2);
        card.add(hintPanel);

        outerPanel.add(card);
        add(outerPanel, BorderLayout.CENTER);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        lblErrorMessage.setText(" ");

        try {
            User user = loginController.login(username, password);
            if (user == null) {
                lblErrorMessage.setText("Invalid username or password.");
                return;
            }

            // Route based on role
            dispose(); // Close login frame
            if (user.isAdmin()) {
                SwingUtilities.invokeLater(() -> new AdminDashboard(user).setVisible(true));
            } else {
                SwingUtilities.invokeLater(() -> new CashierDashboard(user).setVisible(true));
            }

        } catch (Exception ex) {
            lblErrorMessage.setText("Error: " + ex.getMessage());
        }
    }
}
