package utils;

import utils.exceptions.InvalidInputException;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

// small helpers built on lambdas, so the services do not repeat stream().filter().toList()
public class FunctionalUtils {

    /** Returns the items that pass the predicate. */
    public static <T> List<T> filter(List<T> items, Predicate<T> predicate) {
        return items.stream().filter(predicate).toList();
    }

    /** Returns the result of the function for each item, in the same order. */
    public static <T, R> List<R> map(List<T> items, Function<T, R> mapper) {
        return items.stream().map(mapper).toList();
    }

    /** Returns the value if it passes the rule, otherwise throws InvalidInputException with the message. */
    public static <T> T validate(T value, Predicate<T> rule, String errorMessage) {
        if (!rule.test(value)) {
            throw new InvalidInputException(errorMessage);
        }
        return value;
    }
}
