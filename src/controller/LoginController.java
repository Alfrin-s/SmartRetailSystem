package controller;

import dao.UserDAO;
import model.User;
import util.ValidationUtil;

import java.sql.SQLException;

/**
 * Controller managing user authentication and session routing.
 * Follows MVC pattern.
 */
public class LoginController {

    private final UserDAO userDAO;
    private static User currentUser;

    public LoginController() {
        this.userDAO = new UserDAO();
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    /**
     * Authenticates credentials and returns the User object if valid, otherwise null.
     */
    public User login(String username, String password) throws Exception {
        if (!ValidationUtil.isNotEmpty(username)) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (!ValidationUtil.isNotEmpty(password)) {
            throw new IllegalArgumentException("Password is required.");
        }

        try {
            User user = userDAO.authenticate(username.trim(), password);
            if (user != null) {
                currentUser = user;
            }
            return user;
        } catch (SQLException e) {
            throw new Exception("Database error during authentication: " + e.getMessage(), e);
        }
    }
}
