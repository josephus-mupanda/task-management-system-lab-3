import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.UserService;
import utils.ValidationUtils;
import utils.exceptions.InvalidInputException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.createUser("Admin", "admin@gmail.com", 2);
    }

    // rules in ValidationUtils

    @Test
    void testEmptyTextIsRejected() {
        assertEquals("Alpha", ValidationUtils.requireText("  Alpha  ", "Name"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.requireText("   ", "Name"));
    }

    @Test
    void testLettersForNumberAreRejected() {
        InvalidInputException error =
                assertThrows(InvalidInputException.class, () -> ValidationUtils.parseInt("abc", "Hours"));

        assertEquals("Hours must be a whole number.", error.getMessage());
        assertEquals(42, ValidationUtils.parseInt("42", "Hours"));
    }

    @Test
    void testNegativeNumbersAreRejected() {
        assertThrows(InvalidInputException.class, () -> ValidationUtils.requirePositive(-5, "Hours"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.requirePositive(-100.0, "Budget"));
    }

    @Test
    void testMenuChoiceOutOfRangeIsRejected() {
        InvalidInputException error =
                assertThrows(InvalidInputException.class, () -> ValidationUtils.requireChoice(6, 1, 5));

        assertEquals("Invalid option. Please select 1-5.", error.getMessage());
        assertEquals(3, ValidationUtils.requireChoice(3, 1, 5));
    }

    @Test
    void testInvalidStatusIsRejected() {
        assertThrows(InvalidInputException.class, () -> Task.Status.fromText("Done"));
        assertEquals(Task.Status.IN_PROGRESS, Task.Status.fromText("in progress"));
    }

    @Test
    void testIdIsNormalized() {
        assertEquals("PRJ001", ValidationUtils.normalizeId("prj1", "PRJ"));
        assertEquals("PRJ001", ValidationUtils.normalizeId("1", "PRJ"));
        assertEquals("P999", ValidationUtils.normalizeId("P999", "PRJ"));
    }

    // email and role validation

    @Test
    void testValidEmailIsAccepted() {
        assertEquals("name@example.com", ValidationUtils.requireValidEmail(" name@example.com "));
    }

    @Test
    void testInvalidEmailIsRejected() {
        assertThrows(InvalidInputException.class, () -> ValidationUtils.requireValidEmail("plainaddress"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.requireValidEmail("bad@"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.requireValidEmail("no space@gmail.com"));
    }

    @Test
    void testDuplicateEmailIsRejected() {
        assertThrows(InvalidInputException.class,
                () -> userService.createUser("Other Admin", "ADMIN@gmail.com", 1));
    }

    @Test
    void testInvalidRoleIsRejected() {
        InvalidInputException error =
                assertThrows(InvalidInputException.class, () -> userService.createUser("Bob", "bob@gmail.com", 3));

        assertEquals("Role must be 1 (Regular) or 2 (Admin).", error.getMessage());
        assertEquals("Admin", userService.createUser("Bob", "bob@gmail.com", 2).getRole());
    }
}
