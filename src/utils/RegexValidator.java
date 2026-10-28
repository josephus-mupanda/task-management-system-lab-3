package utils;

import utils.exceptions.InvalidInputException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// every format rule is a Pattern here, so there is one place to read and change them
public class RegexValidator {

    public static final Pattern PROJECT_ID = Pattern.compile("P\\d{3}");
    public static final Pattern TASK_ID = Pattern.compile("T\\d{3}");
    public static final Pattern USER_ID = Pattern.compile("U\\d{3}");
    public static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");
    public static final Pattern WHOLE_NUMBER = Pattern.compile("-?\\d+");
    public static final Pattern DECIMAL_NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static final String PROJECT_ID_MESSAGE = "Invalid Project ID format. Use pattern P### (e.g., P001)";
    public static final String TASK_ID_MESSAGE = "Invalid Task ID format. Use pattern T### (e.g., T001)";
    public static final String USER_ID_MESSAGE = "Invalid User ID format. Use pattern U### (e.g., U001)";

    /** Returns true if the whole text matches the pattern. */
    public static boolean matches(Pattern pattern, String text) {
        if (text == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    /** Returns true for ids like P001. */
    public static boolean isProjectId(String text) {
        return matches(PROJECT_ID, text);
    }

    /** Returns true for ids like T001. */
    public static boolean isTaskId(String text) {
        return matches(TASK_ID, text);
    }

    /** Returns true for text like name@example.com. */
    public static boolean isEmail(String text) {
        return matches(EMAIL, text);
    }

    /** Returns the project id, throws InvalidInputException if it is not like P001. */
    public static String requireProjectId(String text) {
        return require(PROJECT_ID, text, PROJECT_ID_MESSAGE);
    }

    /** Returns the task id, throws InvalidInputException if it is not like T001. */
    public static String requireTaskId(String text) {
        return require(TASK_ID, text, TASK_ID_MESSAGE);
    }

    /** Returns the user id, throws InvalidInputException if it is not like U001. */
    public static String requireUserId(String text) {
        return require(USER_ID, text, USER_ID_MESSAGE);
    }

    private static String require(Pattern pattern, String text, String message) {
        if (!matches(pattern, text)) {
            throw new InvalidInputException(message);
        }
        return text;
    }
}
