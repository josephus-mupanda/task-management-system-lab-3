import models.Project;
import services.ProjectService;
import services.ReportService;
import services.StreamService;
import services.TaskService;
import services.UserService;
import utils.ConsoleMenu;
import utils.FileUtils;
import utils.exceptions.DataFileException;
import utils.exceptions.InvalidInputException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    /** Starts the program: loads the data file, opens the menu and saves the data on exit. */
    public static void main(String[] args) {
        ProjectService projectService = new ProjectService();
        TaskService taskService = new TaskService(projectService);
        ReportService reportService = new ReportService(projectService);
        StreamService streamService = new StreamService(projectService);
        UserService userService = new UserService();

        // the users are not saved in the file, so the same two users exist every time the program starts
        userService.createUser("Admin", "admin@gmail.com", 2);
        userService.createUser("Josephus Mupanda", "josephus@gmail.com", 1);

        ConsoleMenu.printHeader("JAVA PROJECT MANAGEMENT SYSTEM v3.0");
        loadData(projectService, userService);

        Scanner scanner = new Scanner(System.in);
        try {
            ConsoleMenu menu = new ConsoleMenu(scanner, projectService, taskService, reportService,
                    streamService, userService);
            menu.start();
        } catch (NoSuchElementException e) {
            // happens when the input is closed (for example Ctrl+D)
            System.out.println("\nInput was closed. Exiting the program.");
        } finally {
            saveData(projectService);
            scanner.close();
        }
    }

    /** Loads the projects of the file. A missing or malformed file is reported and the catalog starts empty. */
    private static void loadData(ProjectService projectService, UserService userService) {
        System.out.println("Loading projects from file...");
        try {
            List<Project> projects = FileUtils.loadProjects(FileUtils.DATA_FILE, userService.getUsers());
            projects.forEach(projectService::addProject);
            System.out.println("✓ " + projects.size() + " projects loaded successfully from projects_data.json");
        } catch (DataFileException | InvalidInputException e) {
            System.out.println("✗ Error: Unable to load projects_data.json (" + e.getMessage() + ")");
            System.out.println("→ Starting with empty catalog.");
        }
    }

    /** Saves every project to the data file. */
    private static void saveData(ProjectService projectService) {
        System.out.println("Saving project data...");
        try {
            FileUtils.saveProjects(projectService.getProjects(), FileUtils.DATA_FILE);
            System.out.println("Data written to projects_data.json successfully!");
        } catch (DataFileException e) {
            System.out.println("✗ Error: Unable to save projects_data.json (" + e.getMessage() + ")");
        }
    }
}
