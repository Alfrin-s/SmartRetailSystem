package view;

import controller.UserController;
import model.User;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * User Management Screen for Administrators.
 * Controls staff accounts and role assignments (ADMIN, CASHIER).
 */
public class UserPanel extends JPanel {

    private final UserController userController;
    private DefaultTableModel tableModel;
    private JTable userTable;
    private List<User> currentUsers;

    public UserPanel() {
        this.userController = new UserController();
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyleUtil.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JLabel lblTitle = new JLabel("USER & STAFF MANAGEMENT (RBAC)");
        lblTitle.setFont(UIStyleUtil.FONT_TITLE);
        lblTitle.setForeground(UIStyleUtil.COLOR_TEXT_PRIMARY);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setBackground(UIStyleUtil.COLOR_BACKGROUND);

        JButton btnAdd = UIStyleUtil.createPrimaryButton("+ Add New User");
        JButton btnEdit = UIStyleUtil.createSecondaryButton("Edit");
        JButton btnDelete = UIStyleUtil.createDangerButton("Delete");
        JButton btnRefresh = UIStyleUtil.createSecondaryButton("Refresh");

        btnAdd.addActionListener(e -> showUserDialog(null));
        btnEdit.addActionListener(e -> editSelectedUser());
        btnDelete.addActionListener(e -> deleteSelectedUser());
        btnRefresh.addActionListener(e -> loadUsers());

        actionsPanel.add(btnAdd);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnDelete);
        actionsPanel.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionsPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Card Table
        JPanel card = UIStyleUtil.createCardPanel();
        card.setLayout(new BorderLayout());

        String[] cols = {"ID", "Username", "Full Name", "Role", "Status", "Created Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        UIStyleUtil.styleTable(userTable);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyleUtil.COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadUsers() {
        try {
            tableModel.setRowCount(0);
            currentUsers = userController.getAllUsers();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            for (User u : currentUsers) {
                String dateStr = (u.getCreatedAt() != null) ? sdf.format(u.getCreatedAt()) : "-";
                tableModel.addRow(new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getFullName(),
                        u.getRole(),
                        u.getStatus(),
                        dateStr
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedUser() {
        int selected = userTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showUserDialog(currentUsers.get(selected));
    }

    private void deleteSelectedUser() {
        int selected = userTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User u = currentUsers.get(selected);
        if ("admin".equalsIgnoreCase(u.getUsername())) {
            JOptionPane.showMessageDialog(this, "Default 'admin' account cannot be deleted.", "Protected Account", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + u.getUsername() + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userController.deleteUser(u.getId());
                loadUsers();
                JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUserDialog(User existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add New User" : "Edit User", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 12, 12));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 24, 10, 24));

        JTextField tfUsername = UIStyleUtil.createTextField(15);
        JPasswordField tfPassword = UIStyleUtil.createPasswordField(15);
        JTextField tfFullName = UIStyleUtil.createTextField(15);
        JComboBox<String> comboRole = new JComboBox<>(new String[]{User.ROLE_CASHIER, User.ROLE_ADMIN});
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{User.STATUS_ACTIVE, User.STATUS_INACTIVE});

        if (existing != null) {
            tfUsername.setText(existing.getUsername());
            tfUsername.setEnabled(false); // Username fixed
            tfFullName.setText(existing.getFullName());
            comboRole.setSelectedItem(existing.getRole());
            comboStatus.setSelectedItem(existing.getStatus());
        }

        formPanel.add(new JLabel("Username:"));
        formPanel.add(tfUsername);
        formPanel.add(new JLabel(existing == null ? "Password:" : "New Password (or blank):"));
        formPanel.add(tfPassword);
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(tfFullName);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(comboRole);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(comboStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        JButton btnSave = UIStyleUtil.createPrimaryButton("Save User");
        JButton btnCancel = UIStyleUtil.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String username = tfUsername.getText().trim();
                String pass = new String(tfPassword.getPassword());
                String fullName = tfFullName.getText().trim();
                String role = (String) comboRole.getSelectedItem();
                String status = (String) comboStatus.getSelectedItem();

                User u = (existing == null) ? new User() : existing;
                u.setUsername(username);
                if (!pass.isEmpty() || existing == null) {
                    u.setPassword(pass);
                }
                u.setFullName(fullName);
                u.setRole(role);
                u.setStatus(status);

                userController.saveUser(u);
                dialog.dispose();
                loadUsers();
                JOptionPane.showMessageDialog(this, "User saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
