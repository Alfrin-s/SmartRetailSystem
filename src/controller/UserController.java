package controller;

import dao.UserDAO;
import model.User;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for user management (Admin only).
 * Follows MVC pattern.
 */
public class UserController {

    private final UserDAO userDAO;

    public UserController() {
        this.userDAO = new UserDAO();
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public boolean saveUser(User user) throws Exception {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username must be at least 3 alphanumeric characters.");
        }
        if (!ValidationUtil.isNotEmpty(user.getFullName())) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }

        try {
            if (user.getId() == null) {
                if (!ValidationUtil.isValidPassword(user.getPassword())) {
                    throw new IllegalArgumentException("Password must be at least 4 characters.");
                }
                return userDAO.createUser(user);
            } else {
                return userDAO.updateUser(user);
            }
        } catch (SQLException e) {
            throw new Exception("Error saving user: " + e.getMessage(), e);
        }
    }

    public boolean deleteUser(int id) throws Exception {
        try {
            return userDAO.deleteUser(id);
        } catch (SQLException e) {
            throw new Exception("Error deleting user: " + e.getMessage(), e);
        }
    }
}
