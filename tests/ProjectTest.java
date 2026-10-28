import models.HardwareProject;
import models.Project;
import models.SoftwareProject;
import models.StatusReport;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.ProjectService;
import services.ReportService;
import services.TaskService;
import utils.exceptions.EmptyProjectException;
import utils.exceptions.InvalidProjectDataException;
import utils.exceptions.ProjectNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectTest {

    private ProjectService projectService;
    private TaskService taskService;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService();
        taskService = new TaskService(projectService);
        reportService = new ReportService(projectService);
    }

    // completion percentage: no tasks, partial, all completed

    @Test
    void testCalculateCompletionPercentage() {
        Project p = new SoftwareProject("P001", "Alpha Tracker", 5, 15000);
        p.addTask(new Task("T001", "Setup DB", "Completed"));
        p.addTask(new Task("T002", "Design UI", "In Progress"));

        double result = p.calculateCompletionPercentage();
        assertEquals(50.0, result);
    }

    @Test
    void testCompletionWithNoTasksThrowsEmptyProjectException() {
        Project p = new SoftwareProject("P001", "Alpha Tracker", 5, 15000);

        assertThrows(EmptyProjectException.class, () -> p.calculateCompletionPercentage());
    }

    @Test
    void testCompletionWithAllTasksCompleted() {
        Project p = new SoftwareProject("P001", "Alpha Tracker", 5, 15000);
        p.addTask(new Task("T001", "Setup DB", "Completed"));
        p.addTask(new Task("T002", "Design UI", "Completed"));

        assertEquals(100.0, p.calculateCompletionPercentage());
    }

    @Test
    void testCompletionIsRoundedToTwoDecimals() {
        Project p = new SoftwareProject("P001", "Alpha Tracker", 5, 15000);
        p.addTask(new Task("T001", "Setup DB", "Completed"));
        p.addTask(new Task("T002", "Design UI", "Pending"));
        p.addTask(new Task("T003", "Write Docs", "Pending"));

        assertEquals(33.33, p.calculateCompletionPercentage());
    }

    // project creation

    @Test
    void testProjectIdIsGenerated() {
        Project first = projectService.createSoftwareProject("Alpha Tracker", "API tracker", 15000, 5, 6, "Java");
        Project second = projectService.createHardwareProject("IoT Sensor Kit", "Sensors", 4000, 2, 4, "ESP32");

        assertEquals("PRJ001", first.getId());
        assertEquals("PRJ002", second.getId());
    }

    @Test
    void testDuplicateProjectIdIsRejected() {
        projectService.addProject(new SoftwareProject("P001", "Alpha Tracker", 5, 15000));

        assertThrows(InvalidProjectDataException.class,
                () -> projectService.addProject(new SoftwareProject("P001", "Another Project", 2, 3000)));
    }

    @Test
    void testNegativeBudgetIsRejected() {
        InvalidProjectDataException error = assertThrows(InvalidProjectDataException.class,
                () -> projectService.createSoftwareProject("Bad Budget", "", -500, 2, 4, "Java"));

        assertEquals("Budget must be greater than 0.", error.getMessage());
    }

    @Test
    void testZeroBudgetIsRejected() {
        assertThrows(InvalidProjectDataException.class,
                () -> new HardwareProject("P002", "IoT Sensor Kit", "", 0, 2, 4, "ESP32"));
    }

    @Test
    void testUnknownProjectIdThrowsProjectNotFoundException() {
        ProjectNotFoundException error =
                assertThrows(ProjectNotFoundException.class, () -> projectService.findProject("P999"));

        assertEquals("Project ID 'P999' does not exist.", error.getMessage());
    }

    // average completion

    @Test
    void testAverageCompletion() {
        Project alpha = projectService.createSoftwareProject("Alpha Tracker", "", 15000, 5, 6, "Java");
        Project iot = projectService.createHardwareProject("IoT Sensor Kit", "", 4000, 2, 4, "ESP32");
        taskService.addTask(alpha, "Task 1", Task.Status.COMPLETED, 1, null);
        taskService.addTask(alpha, "Task 2", Task.Status.COMPLETED, 1, null);
        taskService.addTask(alpha, "Task 3", Task.Status.COMPLETED, 1, null);
        taskService.addTask(alpha, "Task 4", Task.Status.PENDING, 1, null);
        taskService.addTask(iot, "Task 1", Task.Status.COMPLETED, 1, null);
        taskService.addTask(iot, "Task 2", Task.Status.IN_PROGRESS, 1, null);

        StatusReport[] reports = reportService.generateReports();

        assertEquals(62.5, reportService.calculateAverageCompletion(reports));
    }

    @Test
    void testAverageSkipsProjectsWithoutTasks() {
        Project alpha = projectService.createSoftwareProject("Alpha Tracker", "", 15000, 5, 6, "Java");
        projectService.createSoftwareProject("Empty Project", "", 1000, 1, 1, "Java");
        taskService.addTask(alpha, "Task 1", Task.Status.COMPLETED, 1, null);
        taskService.addTask(alpha, "Task 2", Task.Status.PENDING, 1, null);

        StatusReport[] reports = reportService.generateReports();

        assertEquals(50.0, reportService.calculateAverageCompletion(reports));
    }

    @Test
    void testAverageWithoutAnyTasksThrowsEmptyProjectException() {
        projectService.createSoftwareProject("Empty Project", "", 1000, 1, 1, "Java");

        StatusReport[] reports = reportService.generateReports();

        assertThrows(EmptyProjectException.class, () -> reportService.calculateAverageCompletion(reports));
    }
}
