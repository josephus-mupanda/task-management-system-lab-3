package services;

import models.AdminMember;
import models.RegularMember;
import models.User;
import utils.RegexValidator;
import utils.ValidationUtils;
import utils.exceptions.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

// users are kept here and not in ConsoleMenu, so the menu only reads and prints
public class UserService {

    private final List<User> users = new ArrayList<>();
    private int nextNumber = 1;

    /** Creates a user with the next id (U001...). Role 1 = Regular, 2 = Admin. Throws InvalidInputException if a value is not valid. */
    public User createUser(String name, String email, int roleChoice) {
        String validName = ValidationUtils.requireText(name, "Name");
        String validEmail = ValidationUtils.requireValidEmail(email);
        if (emailExists(validEmail)) {
            throw new InvalidInputException("A user with this email already exists.");
        }
        ValidationUtils.requireValidRole(roleChoice);

        String id = String.format("U%03d", nextNumber);
        User user = roleChoice == 2 ? new AdminMember(id, validName, validEmail) : new RegularMember(id, validName, validEmail);

        users.add(user);
        nextNumber++;
        return user;
    }

    /** Returns true if a user already has this email (ignoring case). */
    public boolean emailExists(String email) {
        return users.stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(email.trim()));
    }

    /** Finds a user by id (U001), throws InvalidInputException if the format is wrong, returns null if there is none. */
    public User findUser(String input) {
        String id = RegexValidator.requireUserId(input);
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /** Returns all the users. */
    public List<User> getUsers() {
        return List.copyOf(users);
    }
}
