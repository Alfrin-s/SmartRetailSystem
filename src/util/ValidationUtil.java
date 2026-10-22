package util;

import java.math.BigDecimal;

/**
 * Utility for input validation across the application.
 */
public class ValidationUtil {

    private ValidationUtil() {}

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        return isNotEmpty(username) && username.trim().length() >= 3 && username.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && password.length() >= 4;
    }

    public static boolean isValidBarcode(String barcode) {
        return isNotEmpty(barcode) && barcode.trim().matches("^[a-zA-Z0-9\\-]+$");
    }

    public static boolean isPositiveNumber(String str) {
        if (!isNotEmpty(str)) return false;
        try {
            BigDecimal val = new BigDecimal(str.trim());
            return val.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String str) {
        if (!isNotEmpty(str)) return false;
        try {
            int val = Integer.parseInt(str.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isStrictlyPositiveInteger(String str) {
        if (!isNotEmpty(str)) return false;
        try {
            int val = Integer.parseInt(str.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
