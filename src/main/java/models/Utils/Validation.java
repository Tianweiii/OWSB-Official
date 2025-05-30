package models.Utils;

import java.util.regex.Pattern;

public class Validation {
    // Numeric patterns
    public static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    public static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    public static final Pattern PERCENTAGE_PATTERN = Pattern.compile("^(100|[1-9]?[0-9])(\\.\\d{1,2})?%?$");

    // Text patterns
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]+$");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]\\d{0,14}$");

    // Address pattern - more flexible than name pattern
    public static final Pattern ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9 ,.#/-]+$");

    // Item-specific patterns
    public static final Pattern ITEM_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 &.,()\\\\-]{2,100}$");
    public static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^[a-zA-Z0-9 ,.!?&()-]+$");
    public static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    // Date patterns
    public static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");

    // Business patterns
    public static final Pattern CURRENCY_PATTERN = Pattern.compile("^\\$?\\d+(\\.\\d{2})?$");
    public static final Pattern QUANTITY_PATTERN = Pattern.compile("^[1-9]\\d*$");
    public static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9]{6,12}$");

    public static boolean isValidNumeric(String input) {
        return input != null && NUMERIC_PATTERN.matcher(input).matches();
    }

    public static boolean isValidDecimal(String input) {
        return input != null && DECIMAL_PATTERN.matcher(input).matches();
    }

    public static boolean isValidPercentage(String input) {
        return input != null && PERCENTAGE_PATTERN.matcher(input).matches();
    }

    public static boolean isValidName(String input) {
        return input != null && !input.trim().isEmpty() && NAME_PATTERN.matcher(input.trim()).matches();
    }

    public static boolean isValidEmail(String input) {
        return input != null && EMAIL_PATTERN.matcher(input).matches();
    }

    public static boolean isValidPhone(String input) {
        return input != null && !input.trim().isEmpty() && PHONE_PATTERN.matcher(input.trim()).matches();
    }

    public static boolean isValidDate(String input) {
        return input != null && DATE_PATTERN.matcher(input).matches();
    }

    public static boolean isValidCurrency(String input) {
        return input != null && CURRENCY_PATTERN.matcher(input).matches();
    }

    public static boolean isValidQuantity(String input) {
        return input != null && QUANTITY_PATTERN.matcher(input).matches();
    }

    public static boolean isValidSKU(String input) {
        return input != null && SKU_PATTERN.matcher(input).matches();
    }

    public static boolean isValidAddress(String input) {
        return input != null && !input.trim().isEmpty() &&
                input.trim().length() >= 5 && // Minimum address length
                input.trim().length() <= 200 && // Maximum address length
                ADDRESS_PATTERN.matcher(input.trim()).matches();
    }

    public static boolean isValidItemName(String input) {
        return input != null && !input.trim().isEmpty() &&
                input.trim().length() >= 2 && // Minimum item name length
                input.trim().length() <= 100 && // Maximum item name length
                ITEM_NAME_PATTERN.matcher(input.trim()).matches();
    }

    /**
     * Validates item description - more flexible than item name
     * @param input The description to validate
     * @return true if valid description (can be empty)
     */
    public static boolean isValidDescription(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true; // Description is optional
        }
        return input.trim().length() <= 500 && // Maximum description length
                DESCRIPTION_PATTERN.matcher(input.trim()).matches();
    }

    public static boolean isValidPrice(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        if (!PRICE_PATTERN.matcher(input.trim()).matches()) {
            return false;
        }

        try {
            double price = Double.parseDouble(input.trim());
            return price > 0 && price <= 999999.99; // Reasonable price range
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static boolean isValidCompanyName(String input) {
        return input != null && !input.trim().isEmpty() &&
                input.trim().length() >= 2 &&
                input.trim().length() <= 100 &&
                Pattern.compile("^[a-zA-Z0-9 &.,-]+$").matcher(input.trim()).matches();
    }


    public static boolean isValidSalesQuantity(String input) {
        return isValidQuantity(input) && isValidQuantityRange(input, 1, 9999);
    }

    public static boolean isValidQuantityRange(String input, int min, int max) {
        if (!isValidQuantity(input)) {
            return false;
        }

        try {
            int quantity = Integer.parseInt(input.trim());
            return quantity >= min && quantity <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"']", "");
    }
}