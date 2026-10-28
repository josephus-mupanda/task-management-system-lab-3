import models.Project;
import models.Task;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import services.ProjectService;
import services.TaskService;
import services.UserService;
import utils.FileUtils;
import utils.exceptions.DataFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilePersistenceTest {

    // JUnit creates an empty folder for each test, so the real data file is never touched
    @TempDir
    Path folder;

    private Path file;
    private List<User> users;
    private ProjectService projectService;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        file = folder.resolve("projects_data.json");
        UserService userService = new UserService();
        userService.createUser("Admin", "admin@gmail.com", 2);
        users = userService.getUsers();
        projectService = new ProjectService();
        taskService = new TaskService(projectService);
    }

    // Feature 3 and US-3.1: save with writeString, load with readAllLines, the data is the same after a restart

    @Test
    void testSaveThenLoadKeepsTheProjectsAndTasks() {
        Project alpha = projectService.createSoftwareProject("P001", "Alpha Tracker", "API tracker", 15000, 5, 6, "Java");
        projectService.createHardwareProject("P002", "IoT Sensor Kit", "Sensors", 4000, 2, 4, "ESP32");
        taskService.addTask(alpha, "T001", "Design DB", Task.Status.COMPLETED, 6, null);

        FileUtils.saveProjects(projectService.getProjects(), file);
        List<Project> loaded = FileUtils.loadProjects(file, users);

        assertEquals(2, loaded.size());
        assertEquals("Alpha Tracker", loaded.get(0).getName());
        assertEquals("Completed", loaded.get(0).findTask("T001").getStatus());
        assertEquals("Hardware", loaded.get(1).getType());
    }

    // a budget of 10 million was saved as 1.0E7, the next start could not read it and the projects were lost
    @Test
    void testBigBudgetIsLoadedBack() {
        projectService.createSoftwareProject("P001", "Alpha Tracker", "", 10000000, 5, 6, "Java");

        FileUtils.saveProjects(projectService.getProjects(), file);
        List<Project> loaded = FileUtils.loadProjects(file, users);

        assertEquals(10000000, loaded.get(0).getBudget());
    }

    // Feature 3: functional reading with Files.lines(), used to confirm the write

    @Test
    void testCountProjectsReadsTheSavedFile() {
        projectService.createSoftwareProject("P001", "Alpha Tracker", "", 1000, 1, 1, "Java");
        projectService.createHardwareProject("P002", "IoT Sensor Kit", "", 4000, 2, 4, "ESP32");

        FileUtils.saveProjects(projectService.getProjects(), file);

        assertEquals(2, FileUtils.countProjects(file));
    }

    // Test Scenario 6: missing or malformed files are handled without crashing

    @Test
    void testMissingFileThrowsDataFileException() {
        DataFileException error = assertThrows(DataFileException.class,
                () -> FileUtils.loadProjects(folder.resolve("missing.json"), users));

        assertEquals("File not found", error.getMessage());
    }

    @Test
    void testMalformedFileThrowsDataFileException() throws IOException {
        Files.writeString(file, "this is not json");

        assertThrows(DataFileException.class, () -> FileUtils.loadProjects(file, users));
    }
}
