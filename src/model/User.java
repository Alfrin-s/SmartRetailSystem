package model;

/**
 * User entity representing an application user with Role-Based Access Control (RBAC).
 * Demonstrates Encapsulation and Inheritance.
 */
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CASHIER = "CASHIER";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    private String username;
    private String password;
    private String fullName;
    private String role;
    private String status;

    public User() {
        super();
        this.role = ROLE_CASHIER;
        this.status = STATUS_ACTIVE;
    }

    public User(Integer id, String username, String password, String fullName, String role, String status) {
        super(id);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = (role != null) ? role.toUpperCase() : ROLE_CASHIER;
        this.status = (status != null) ? status.toUpperCase() : STATUS_ACTIVE;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role != null) ? role.toUpperCase() : ROLE_CASHIER;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status != null) ? status.toUpperCase() : STATUS_ACTIVE;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.role);
    }

    public boolean isCashier() {
        return ROLE_CASHIER.equalsIgnoreCase(this.role);
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
