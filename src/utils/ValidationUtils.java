package utils;

import utils.exceptions.InvalidInputException;

// rules only, no keyboard reading, so each rule can be tested on its own
public class ValidationUtils {

    /** Checks that the text is not empty and returns it without the extra spaces. */
    public static String requireText(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        return text.trim();
    }

    /** Turns the text into a whole number, throws InvalidInputException if it is not one. */
    public static int parseInt(String text, String fieldName) {
        if (text == null || !RegexValidator.matches(RegexValidator.WHOLE_NUMBER, text.trim())) {
            throw new InvalidInputException(fieldName + " must be a whole number.");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            // matches the pattern but does not fit in an int
            throw new InvalidInputException(fieldName + " must be a whole number.");
        }
    }

    /** Turns the text into a decimal number, throws InvalidInputException if it is not one. */
    public static double parseDouble(String text, String fieldName) {
        if (text == null || !RegexValidator.matches(RegexValidator.DECIMAL_NUMBER, text.trim())) {
            throw new InvalidInputException(fieldName + " must be a number.");
        }
        return Double.parseDouble(text.trim());
    }

    /** Checks that the number is greater than 0. */
    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new InvalidInputException(fieldName + " must be greater than 0.");
        }
        return value;
    }

    /** Checks that the number is greater than 0. */
    public static double requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new InvalidInputException(fieldName + " must be greater than 0.");
        }
        return value;
    }

    /** Checks that a menu choice is between min and max. */
    public static int requireChoice(int choice, int min, int max) {
        if (choice < min || choice > max) {
            throw new InvalidInputException("Invalid option. Please select " + min + "-" + max + ".");
        }
        return choice;
    }

    /** Checks that the email looks like name@example.com. */
    public static String requireValidEmail(String email) {
        String value = requireText(email, "Email");
        if (!RegexValidator.isEmail(value)) {
            throw new InvalidInputException("Invalid email format. Use name@example.com (e.g., josephus@gmail.com)");
        }
        return value;
    }

    /** Checks that the role choice is 1 (Regular) or 2 (Admin). */
    public static int requireValidRole(int roleChoice) {
        if (roleChoice != 1 && roleChoice != 2) {
            throw new InvalidInputException("Role must be 1 (Regular) or 2 (Admin).");
        }
        return roleChoice;
    }

    /** Checks that the minimum budget is not negative and not bigger than the maximum. */
    public static void requireValidRange(double min, double max) {
        if (min < 0 || max < min) {
            throw new InvalidInputException("Minimum must be 0 or more and not above maximum.");
        }
    }
}
