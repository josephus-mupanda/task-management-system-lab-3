package utils;

import interfaces.TaskFilter;
import models.Project;
import models.StatusReport;
import models.Task;
import models.User;
import services.ProjectService;
import services.ConcurrencyService;
import services.ReportService;
import services.StreamService;
import services.TaskService;
import services.UserService;
import utils.exceptions.EmptyProjectException;
import utils.exceptions.InvalidInputException;
import utils.exceptions.ProjectNotFoundException;
import utils.exceptions.TaskNotFoundException;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

// the only class that reads the keyboard and prints, so the services can be tested without a console
public class ConsoleMenu {

    public static final String LINE = "─".repeat(80);

    private static final int BOX_WIDTH = 40;

    // 70 comes from the lab example: projects.values().stream().filter(p -> p.getCompletionRate() > 70)
    private static final int COMPLETION_LIMIT = 70;

    private final Scanner scanner;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final ReportService reportService;
    private final StreamService streamService;
    private final ConcurrencyService concurrencyService;
    private final UserService userService;
    private User currentUser;

    /** Creates the menu, the first user of the user service is logged in. */
    public ConsoleMenu(Scanner scanner, ProjectService projectService, TaskService taskService,
                       ReportService reportService, StreamService streamService,
                       ConcurrencyService concurrencyService,
                       UserService userService) {
        this.scanner = scanner;
        this.projectService = projectService;
        this.taskService = taskService;
        this.reportService = reportService;
        this.streamService = streamService;
        this.concurrencyService = concurrencyService;
        this.userService = userService;
        this.currentUser = userService.getUsers().get(0);
    }

    // ---------- printing utility methods ----------

    /** Prints a title inside a box. */
    public static void printHeader(String title) {
        int left = (BOX_WIDTH - title.length()) / 2;
        int right = BOX_WIDTH - title.length() - left;

        System.out.println();
        System.out.println("╔" + "═".repeat(BOX_WIDTH) + "╗");
        System.out.println("║" + " ".repeat(left) + title + " ".repeat(right) + "║");
        System.out.println("╚" + "═".repeat(BOX_WIDTH) + "╝");
        System.out.println();
    }

    /** Prints an error like "✗ Error: Task 'T999' was not found in the project." */
    public static void printError(RuntimeException error) {
        System.out.println("✗ Error: " + error.getMessage());
    }

    /** Prints a message with a check mark. */
    public static void printSuccess(String message) {
        System.out.println();
        System.out.println("✓ " + message);
    }

    /** Formats a percentage: 75.0 gives "75%" and 62.5 gives "62.5%". */
    public static String formatPercent(double value) {
        return new DecimalFormat("0.##").format(value) + "%";
    }

    /** Formats a budget: 8000 gives "8,000". */
    public static String formatBudget(double budget) {
        if (budget == Math.floor(budget)) {
            return String.format("%,.0f", budget);
        }
        return String.format("%,.2f", budget);
    }

    /** Centers the text in a column of the given width. */
    public static String center(String text, int width) {
        int left = (width - text.length()) / 2;
        return " ".repeat(left) + text + " ".repeat(width - text.length() - left);
    }

    // ---------- reading input: each method asks again until the value is valid ----------

    private void showRetry(InvalidInputException error) {
        printError(error);
        System.out.println("Please try again.");
        System.out.println();
    }

    private void showAbort(RuntimeException error, String menuName) {
        printError(error);
        System.out.println();
        System.out.println("Operation aborted. Returning to " + menuName + "...");
    }

    private int readChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int choice = ValidationUtils.parseInt(scanner.nextLine(), "Choice");
                return ValidationUtils.requireChoice(choice, min, max);
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    private String readText(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            try {
                return ValidationUtils.requireText(scanner.nextLine(), fieldName);
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    private int readWholeNumber(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            try {
                return ValidationUtils.parseInt(scanner.nextLine(), fieldName);
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    private int readPositiveInt(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = ValidationUtils.parseInt(scanner.nextLine(), fieldName);
                return ValidationUtils.requirePositive(value, fieldName);
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    private double readNumber(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            try {
                return ValidationUtils.parseDouble(scanner.nextLine(), fieldName);
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    private Task.Status readStatus(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Task.Status.fromText(scanner.nextLine());
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    private String readEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String email = ValidationUtils.requireValidEmail(scanner.nextLine());
                if (userService.emailExists(email)) {
                    throw new InvalidInputException("A user with this email already exists.");
                }
                return email;
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    // a wrong id is a typing mistake, so we ask again instead of aborting the whole operation
    private String readNewProjectId(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String id = RegexValidator.requireProjectId(scanner.nextLine().trim());
                if (projectService.idExists(id)) {
                    throw new InvalidInputException("Project ID '" + id + "' already exists.");
                }
                return id;
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    // same reason as readNewProjectId: a typing mistake is easy to fix by typing again
    private String readNewTaskId(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String id = RegexValidator.requireTaskId(scanner.nextLine().trim());
                if (!taskService.isIdAvailable(id)) {
                    throw new InvalidInputException("Task ID '" + id + "' already exists.");
                }
                return id;
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    private int readRole(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int role = ValidationUtils.parseInt(scanner.nextLine(), "Role");
                return ValidationUtils.requireValidRole(role);
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    // returns null when the user types 0, so the caller can cancel without an exception
    private Project askForProject(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            try {
                Project project = projectService.findProject(input);
                printSuccess("Project '" + project.getName() + "' loaded successfully.");
                return project;
            } catch (ProjectNotFoundException e) {
                printError(e);
                System.out.println("Please try again.");
                System.out.println();
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    // returns null when the user types 0, so a task can be left unassigned
    private User askForUser(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            try {
                User user = userService.findUser(input);
                if (user == null) {
                    throw new InvalidInputException("User '" + input + "' does not exist.");
                }
                return user;
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    // ---------- main menu ----------

    /** Shows the main menu until the user chooses Exit. */
    public void start() {
        int choice;

        do {
            displayMainMenu();
            choice = readChoice("Enter your choice: ", 1, 6);

            try {
                switch (choice) {
                    case 1:
                        projectMenu();
                        break;
                    case 2:
                        taskMenu();
                        break;
                    case 3:
                        showStatusReport();
                        break;
                    case 4:
                        simulateConcurrentUpdates();
                        break;
                    case 5:
                        switchUserMenu();
                        break;
                    default:
                        System.out.println("\nGoodbye.");
                }
            } catch (InvalidInputException | ProjectNotFoundException | TaskNotFoundException | EmptyProjectException e) {
                showAbort(e, "main menu");
            }
        } while (choice != 6);
    }

    private void displayMainMenu() {
        printHeader("JAVA PROJECT MANAGEMENT SYSTEM v3.0");
        System.out.println("Current User: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        System.out.println();
        System.out.println("Main Menu:");
        System.out.println("-----------");
        System.out.println("1. Manage Projects");
        System.out.println("2. Manage Tasks");
        System.out.println("3. View Status Reports");
        System.out.println("4. Simulate Concurrent Updates");
        System.out.println("5. Switch User");
        System.out.println("6. Exit");
        System.out.println();
    }

    // one check for every menu, so the admin rule is written only once
    private boolean requireModifyAccess(String action) {
        if (currentUser.canModify()) {
            return true;
        }
        System.out.println("✗ Error: Access denied. Only admins can " + action + ".");
        return false;
    }

    // ---------- projects ----------

    private void projectMenu() {
        int choice;

        do {
            printHeader("MANAGE PROJECTS");
            System.out.println("1. Browse Project Catalog");
            System.out.println("2. Add New Project");
            System.out.println("3. Remove Project");
            System.out.println("4. Back to Main Menu");
            System.out.println();

            choice = readChoice("Enter your choice: ", 1, 4);

            try {
                switch (choice) {
                    case 1:
                        browseCatalog();
                        break;
                    case 2:
                        addProject();
                        break;
                    case 3:
                        removeProject();
                        break;
                    default:
                        break;
                }
            } catch (InvalidInputException | ProjectNotFoundException | TaskNotFoundException | EmptyProjectException e) {
                showAbort(e, "project menu");
            }
        } while (choice != 4);
    }

    private void removeProject() {
        if (!requireModifyAccess("remove projects")) {
            return;
        }

        displayProjectList();
        Project project = askForProject("Enter project ID to remove (or 0 to cancel): ");
        if (project == null) {
            return;
        }
        projectService.removeProject(project.getId());
        printSuccess("Project '" + project.getName() + "' removed with its " + project.getTaskCount() + " task(s).");
    }

    private void browseCatalog() {
        while (true) {
            printHeader("PROJECT CATALOG");
            System.out.println("Filter Options:");
            System.out.println("1. View All Projects (" + projectService.getProjects().size() + ")");
            System.out.println("2. Software Projects Only");
            System.out.println("3. Hardware Projects Only");
            System.out.println("4. Search by Budget Range");
            System.out.println("5. Completed Projects (above " + COMPLETION_LIMIT + "%)");
            System.out.println("0. Back");
            System.out.println();

            int choice = readChoice("Enter filter choice: ", 0, 5);
            List<Project> list;

            switch (choice) {
                case 0:
                    return;
                case 1:
                    list = projectService.getProjects();
                    break;
                case 2:
                    list = projectService.getByType("Software");
                    break;
                case 3:
                    list = projectService.getByType("Hardware");
                    break;
                case 4:
                    list = searchByBudget();
                    break;
                default:
                    list = streamService.projectsAboveCompletion(COMPLETION_LIMIT);
            }

            System.out.println();
            displayProjects(list);
            if (!list.isEmpty()) {
                Project project = askForProject("Enter project ID to view details (or 0 to return): ");
                if (project != null) {
                    projectDetails(project);
                }
            }
        }
    }

    private List<Project> searchByBudget() {
        while (true) {
            double min = readNumber("Enter minimum budget: ", "Minimum budget");
            double max = readNumber("Enter maximum budget: ", "Maximum budget");
            try {
                ValidationUtils.requireValidRange(min, max);
                return projectService.getByBudgetRange(min, max);
            } catch (InvalidInputException e) {
                showRetry(e);
            }
        }
    }

    // no budget or team size check here: the Project constructor already refuses wrong values
    private void addProject() {
        printHeader("ADD NEW PROJECT");
        System.out.println("1. Software Project");
        System.out.println("2. Hardware Project");
        System.out.println();

        int type = readChoice("Choose project type: ", 1, 2);

        String id = readNewProjectId("Enter new project ID: ");
        String name = readText("Enter project name: ", "Project name");
        while (projectService.nameExists(name)) {
            System.out.println("✗ Error: A project named '" + name + "' already exists.");
            System.out.println("Please try again.");
            System.out.println();
            name = readText("Enter project name: ", "Project name");
        }

        String description = readText("Enter description: ", "Description");
        double budget = readNumber("Enter budget: ", "Budget");
        int teamSize = readWholeNumber("Enter team size: ", "Team size");
        int duration = readWholeNumber("Enter duration in weeks: ", "Duration");

        Project project;
        if (type == 1) {
            String techStack = readText("Enter tech stack: ", "Tech stack");
            project = projectService.createSoftwareProject(id, name, description, budget, teamSize, duration, techStack);
        } else {
            String deviceType = readText("Enter device type: ", "Device type");
            project = projectService.createHardwareProject(id, name, description, budget, teamSize, duration, deviceType);
        }

        printSuccess("Project '" + project.getName() + "' successfully created (ID: " + project.getId() + ").");
    }

    private void projectDetails(Project project) {
        int choice;

        do {
            showProject(project);

            System.out.println("\nOptions:");
            System.out.println("1. Add New Task");
            System.out.println("2. Update Task Status");
            System.out.println("3. Remove Task");
            System.out.println("4. Back to Project Catalog");
            System.out.println();

            choice = readChoice("Enter your choice: ", 1, 4);

            try {
                switch (choice) {
                    case 1:
                        addTask(project);
                        break;
                    case 2:
                        updateTaskStatus(project);
                        break;
                    case 3:
                        removeTask(project);
                        break;
                    default:
                        break;
                }
            } catch (InvalidInputException | ProjectNotFoundException | TaskNotFoundException | EmptyProjectException e) {
                showAbort(e, "project details");
            }
        } while (choice != 4);
    }

    private void showProject(Project project) {
        printHeader("PROJECT DETAILS: " + project.getId());

        System.out.println("Project Name: " + project.getName());
        System.out.println("Type: " + project.getType());
        System.out.println(project.getProjectDetails());
        System.out.println("Team Size: " + project.getTeamSize());
        System.out.println("Duration: " + project.getDurationWeeks() + " weeks");
        System.out.println("Budget: $" + formatBudget(project.getBudget()));
        System.out.println("\nAssociated Tasks:");
        displayTasks(project);
        System.out.println("Completion Rate: " + completionText(project));
    }

    // ---------- tasks ----------

    private void taskMenu() {
        int choice;

        do {
            printHeader("MANAGE TASKS");
            System.out.println("1. Add New Task");
            System.out.println("2. View Tasks by Project");
            System.out.println("3. Update Task Status");
            System.out.println("4. Remove Task");
            System.out.println("5. Calculate Project Completion");
            System.out.println("6. Back to Main Menu");
            System.out.println();

            choice = readChoice("Enter your choice: ", 1, 6);

            try {
                switch (choice) {
                    case 1:
                        addTask(null);
                        break;
                    case 2:
                        viewTasks();
                        break;
                    case 3:
                        updateTaskStatus(null);
                        break;
                    case 4:
                        removeTask(null);
                        break;
                    case 5:
                        calculateCompletion();
                        break;
                    default:
                        break;
                }
            } catch (InvalidInputException | ProjectNotFoundException | TaskNotFoundException | EmptyProjectException e) {
                showAbort(e, "task menu");
            }
        } while (choice != 6);
    }

    // no hasTasks() check here: EmptyProjectException goes up to the task menu, which shows the error
    private void calculateCompletion() {
        displayProjectList();

        Project project = askForProject("Enter project ID (or 0 to cancel): ");
        if (project == null) {
            return;
        }

        System.out.println("\nProject: " + project.getName());
        System.out.println("Total tasks: " + project.getTaskCount());
        System.out.println("Completed tasks: " + project.countByStatus(Task.Status.COMPLETED));
        System.out.println("In progress tasks: " + project.countByStatus(Task.Status.IN_PROGRESS));
        System.out.println("Pending tasks: " + project.countByStatus(Task.Status.PENDING));
        System.out.println("Completion: " + formatPercent(project.calculateCompletionPercentage()));
    }

    private void viewTasks() {
        displayProjectList();

        Project project = askForProject("Enter project ID (or 0 to cancel): ");
        if (project == null) {
            return;
        }

        System.out.println("\nProject: " + project.getName());
        displayTasks(project);
        System.out.println("Completion Rate: " + completionText(project));
    }

    // scope is null from the task menu and set from project details, so one method serves both screens
    private void addTask(Project scope) {
        printHeader("ADD NEW TASK");

        Project project = scope;
        if (project == null) {
            project = askForProject("Enter project ID (or 0 to cancel): ");
            if (project == null) {
                return;
            }
        }

        String id = readNewTaskId("Enter new task ID: ");
        String name = readText("Enter task name: ", "Task name");
        Task.Status status = readStatus("Enter status (Pending/In Progress/Completed): ");
        int hours = readPositiveInt("Enter estimated hours: ", "Hours");

        System.out.println();
        displayUsers();
        User assignee = askForUser("Enter assigned user ID (or 0 for nobody): ");

        Task task = taskService.addTask(project, id, name, status, hours, assignee);
        printSuccess("Task \"" + task.getName() + "\" added successfully.");
    }

    // no check here: TaskNotFoundException goes up to the menu, which shows the error
    private void updateTaskStatus(Project scope) {
        if (!requireModifyAccess("update tasks")) {
            return;
        }

        String taskId = readText("Enter task ID: ", "Task ID");
        Project owner = ownerOf(scope, taskId);
        Task task = taskService.findTask(owner, taskId);

        Task.Status status = readStatus("Enter new status (Pending/In Progress/Completed): ");
        taskService.updateTaskStatus(owner, taskId, status);
        printSuccess("Task '" + task.getName() + "' marked as " + status + ".");
    }

    private void removeTask(Project scope) {
        if (!requireModifyAccess("delete tasks")) {
            return;
        }

        String taskId = readText("Enter task ID: ", "Task ID");
        Project owner = ownerOf(scope, taskId);
        Task removed = taskService.removeTask(owner, taskId);
        printSuccess("Task '" + removed.getName() + "' removed from Project " + owner.getId() + ".");
    }

    // from the task menu we only know the task id, so the project that holds it must be searched
    private Project ownerOf(Project scope, String taskId) {
        if (scope != null) {
            return scope;
        }
        return taskService.findProjectOfTask(taskId);
    }

    // ---------- concurrent updates ----------

    // completed tasks are left out because they have no next status
    private void simulateConcurrentUpdates() {
        if (!requireModifyAccess("update tasks")) {
            return;
        }

        printHeader("PARALLEL TASK UPDATE SIMULATION");

        // a lambda is enough for a one-line rule, no class is needed
        TaskFilter notCompleted = task -> !task.isCompleted();
        List<Task> tasks = streamService.filterTasks(notCompleted);
        if (tasks.isEmpty()) {
            System.out.println("No tasks to update: all the tasks are already completed.");
            return;
        }

        System.out.println("1. Threads");
        System.out.println("2. Parallel stream");
        System.out.println();
        int method = readChoice("Enter your choice: ", 1, 2);

        if (method == 1) {
            System.out.println("Starting " + tasks.size() + " threads...");
            try {
                concurrencyService.updateWithThreads(tasks);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("✗ Error: The updates were interrupted.");
                return;
            }
            System.out.println("All threads finished successfully.");
        } else {
            System.out.println("Starting parallel stream...");
            concurrencyService.updateWithParallelStream(tasks);
            System.out.println("Parallel stream finished successfully.");
        }
        printSuccess("Task updates applied concurrently and safely.");
    }

    // ---------- users ----------

    private void switchUserMenu() {
        int choice;

        do {
            printHeader("SWITCH USER");
            System.out.println("Current User: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
            System.out.println();
            displayUsers();
            System.out.println("1. Switch to another user");
            System.out.println("2. Create a new user");
            System.out.println("3. Back to Main Menu");
            System.out.println();

            choice = readChoice("Enter your choice: ", 1, 3);

            try {
                switch (choice) {
                    case 1:
                        switchUser();
                        break;
                    case 2:
                        createUser();
                        break;
                    default:
                        break;
                }
            } catch (InvalidInputException | ProjectNotFoundException | TaskNotFoundException | EmptyProjectException e) {
                showAbort(e, "user menu");
            }
        } while (choice != 3);
    }

    private void switchUser() {
        User user = askForUser("Enter user ID (or 0 to cancel): ");
        if (user == null) {
            return;
        }
        currentUser = user;
        printSuccess("Now logged in as " + user.getName() + " (" + user.getRole() + ").");
    }

    private void createUser() {
        if (!requireModifyAccess("create users")) {
            return;
        }

        String name = readText("Enter name: ", "Name");
        String email = readEmail("Enter email: ");
        System.out.println("1. Regular User");
        System.out.println("2. Admin User");
        int role = readRole("Choose role: ");

        User user = userService.createUser(name, email, role);
        printSuccess("User " + user.getName() + " created successfully (ID: " + user.getId() + ").");
    }

    // ---------- tables and reports ----------

    private void displayProjects(List<Project> list) {
        if (list.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        String line = "─".repeat(66);
        System.out.println(line);
        System.out.printf(" %-6s │ %-20s │ %-9s │ %-5s │ %s%n", "ID", "PROJECT NAME", "TYPE", "TASKS", "COMPLETED (%)");
        System.out.println(line);
        list.forEach(this::displayProjectRow);
        System.out.println(line);
        System.out.println("Total projects: " + list.size());
    }

    private void displayProjectRow(Project project) {
        System.out.printf(" %-6s │ %-20s │ %-9s │ %-5d │ %s%n",
                project.getId(), project.getName(), project.getType(), project.getTaskCount(),
                project.hasTasks() ? formatPercent(project.calculateCompletionPercentage()) : "N/A");
    }

    private void displayProjectList() {
        System.out.println();
        for (Project project : projectService.getProjects()) {
            System.out.println("  " + project.getId() + "  " + project.getName() + " (" + project.getType() + ")");
        }
        System.out.println();
    }

    private void displayTasks(Project project) {
        List<Task> tasks = project.getTasks();

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        System.out.println(LINE);
        System.out.printf("%-6s | %-20s | %-11s | %-5s | %s%n", "ID", "TASK NAME", "STATUS", "HOURS", "ASSIGNED TO");
        System.out.println(LINE);

        for (Task task : tasks) {
            String assignee = task.getAssignedTo() == null ? "Unassigned" : task.getAssignedTo().getName();
            System.out.printf("%-6s | %-20s | %-11s | %-5d | %s%n",
                    task.getId(), task.getName(), task.getStatus(), task.getHours(), assignee);
        }
        System.out.println(LINE);
    }

    private void displayUsers() {
        System.out.println(LINE);
        System.out.printf("%-4s | %-20s | %-8s | %s%n", "ID", "NAME", "ROLE", "EMAIL");
        System.out.println(LINE);

        for (User user : userService.getUsers()) {
            System.out.printf("%-4s | %-20s | %-8s | %s%n",
                    user.getId(), user.getName(), user.getRole(), user.getEmail());
        }
        System.out.println(LINE);
    }

    private String completionText(Project project) {
        if (!project.hasTasks()) {
            return "N/A (no tasks yet)";
        }
        return formatPercent(project.calculateCompletionPercentage());
    }

    private void showStatusReport() {
        printHeader("PROJECT STATUS REPORT");

        List<StatusReport> reports = reportService.generateReports();
        if (reports.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        System.out.println(LINE);
        System.out.printf("%-10s | %-20s | %-5s | %-9s | %s%n",
                "PROJECT ID", "PROJECT NAME", "TASKS", "COMPLETED", "PROGRESS (%)");
        System.out.println(LINE);

        reports.forEach(report -> System.out.printf("%-10s | %-20s | %s | %s | %s%n",
                report.getProjectId(),
                report.getProjectName(),
                center(String.valueOf(report.getTotalTasks()), 5),
                center(String.valueOf(report.getCompletedTasks()), 9),
                report.hasTasks() ? formatPercent(report.getPercentage()) : "N/A"));
        System.out.println(LINE);

        try {
            double average = reportService.calculateAverageCompletion(reports);
            System.out.println("AVERAGE COMPLETION: " + formatPercent(average));
        } catch (EmptyProjectException e) {
            System.out.println("AVERAGE COMPLETION: N/A");
            printError(e);
        }

        // the totals are taken from the reports, so they always match the table above
        System.out.println("\nTask Statistics:");
        System.out.println("Total: " + reports.stream().mapToInt(StatusReport::getTotalTasks).sum());
        System.out.println("Completed: " + reports.stream().mapToInt(StatusReport::getCompletedTasks).sum());
        System.out.println("In Progress: " + reports.stream().mapToInt(StatusReport::getInProgressTasks).sum());
        System.out.println("Pending: " + reports.stream().mapToInt(StatusReport::getPendingTasks).sum());

        System.out.println("\nSummary:");
        reports.forEach(report -> System.out.println(report.hasTasks()
                ? report.getProjectName() + " is " + formatPercent(report.getPercentage()) + " complete"
                : report.getProjectName() + " has no tasks yet"));

        printSuccess("Report generated successfully.");
    }
}
