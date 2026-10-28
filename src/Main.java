import models.Project;
import models.Task;
import models.User;
import services.ProjectService;
import services.ReportService;
import services.StreamService;
import services.TaskService;
import services.UserService;
import utils.ConsoleMenu;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    /** Starts the program: creates the services, loads the sample data and opens the menu. */
    public static void main(String[] args) {
        ProjectService projectService = new ProjectService();
        TaskService taskService = new TaskService(projectService);
        ReportService reportService = new ReportService(projectService);
        StreamService streamService = new StreamService(projectService);
        UserService userService = new UserService();

        Scanner scanner = new Scanner(System.in);

        try {
            loadSampleData(projectService, taskService, userService);
            ConsoleMenu menu = new ConsoleMenu(scanner, projectService, taskService, reportService,
                    streamService, userService);
            menu.start();
        } catch (NoSuchElementException e) {
            // happens when the input is closed (for example Ctrl+D)
            System.out.println("\nInput was closed. Exiting the program.");
        } finally {
            scanner.close();
        }
    }

    /** Adds the users, projects and tasks that exist when the program starts. */
    private static void loadSampleData(ProjectService projectService, TaskService taskService,
                                       UserService userService) {
        User admin = userService.createUser("Admin", "admin@gmail.com", 2);
        User josephus = userService.createUser("Josephus Mupanda", "josephus@gmail.com", 1);

        Project budgetBuddy = projectService.createSoftwareProject("P001",
                "Budget Buddy", "Personal expense and savings tracker", 8000, 3, 12, "Java");
        Project gardenMonitor = projectService.createHardwareProject("P002",
                "Smart Garden Monitor", "Soil moisture sensor that waters my plants", 5000, 2, 8,
                "Arduino sensor board");
        Project portfolio = projectService.createSoftwareProject("P003",
                "Portfolio Website", "My personal website to show my projects", 3000, 1, 6,
                "HTML, CSS and JavaScript");
        projectService.createHardwareProject("P004",
                "Home Power Meter", "Device that tracks electricity use at home", 12000, 3, 10,
                "Energy meter module");
        Project studyPlanner = projectService.createSoftwareProject("P005",
                "Study Planner", "Timetable and revision planner for my courses", 4500, 2, 8, "Java");

        taskService.addTask(budgetBuddy, "T001", "Design Database", Task.Status.COMPLETED, 6, null);
        taskService.addTask(budgetBuddy, "T002", "Build Expense Screen", Task.Status.COMPLETED, 10, josephus);
        taskService.addTask(budgetBuddy, "T003", "Add Savings Goals", Task.Status.COMPLETED, 8, null);
        taskService.addTask(budgetBuddy, "T004", "Write Unit Tests", Task.Status.COMPLETED, 5, admin);
        taskService.addTask(budgetBuddy, "T005", "Publish Release", Task.Status.PENDING, 3, null);
        taskService.addTask(gardenMonitor, "T006", "Wire Moisture Sensor", Task.Status.COMPLETED, 4, null);
        taskService.addTask(gardenMonitor, "T007", "Calibrate Water Pump", Task.Status.PENDING, 3, null);
        taskService.addTask(portfolio, "T008", "Design Layout", Task.Status.COMPLETED, 4, null);
        taskService.addTask(portfolio, "T009", "Write About Page", Task.Status.IN_PROGRESS, 2, josephus);
        taskService.addTask(portfolio, "T010", "Deploy Site", Task.Status.PENDING, 1, null);
        taskService.addTask(studyPlanner, "T011", "Build Timetable", Task.Status.COMPLETED, 5, null);
        taskService.addTask(studyPlanner, "T012", "Add Revision Reminders", Task.Status.COMPLETED, 3, admin);
    }
}
