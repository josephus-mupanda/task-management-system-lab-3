import models.Project;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.ProjectService;
import services.TaskService;
import utils.exceptions.InvalidInputException;
import utils.exceptions.TaskNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskTest {

    private ProjectService projectService;
    private TaskService taskService;
    private Project alpha;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService();
        taskService = new TaskService(projectService);
        alpha = projectService.createSoftwareProject("Alpha Tracker", "API tracker", 15000, 5, 6, "Java");
    }

    // adding tasks

    @Test
    void testAddTaskToProject() {
        Task task = taskService.addTask(alpha, "Implement API", Task.Status.IN_PROGRESS, 8, null);

        assertEquals("TSK001", task.getId());
        assertEquals(1, alpha.getTaskCount());
    }

    @Test
    void testDuplicateTaskNameIsRejected() {
        taskService.addTask(alpha, "Implement API", Task.Status.PENDING, 8, null);

        assertThrows(InvalidInputException.class,
                () -> taskService.addTask(alpha, "implement api", Task.Status.PENDING, 2, null));
        assertEquals(1, alpha.getTaskCount());
    }

    @Test
    void testNegativeHoursAreRejected() {
        assertThrows(InvalidInputException.class,
                () -> taskService.addTask(alpha, "Implement API", Task.Status.PENDING, -3, null));
    }

    // status updates and progress

    @Test
    void testUpdateTaskStatus() {
        taskService.addTask(alpha, "Setup DB", Task.Status.PENDING, 4, null);
        taskService.addTask(alpha, "Design UI", Task.Status.PENDING, 6, null);

        Task updated = taskService.updateTaskStatus(alpha, "TSK001", Task.Status.COMPLETED);

        assertEquals(Task.Status.COMPLETED, updated.getStatus());
        assertEquals(50.0, alpha.calculateCompletionPercentage());
    }

    @Test
    void testUpdateUnknownTaskThrowsTaskNotFoundException() {
        TaskNotFoundException error = assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTaskStatus(alpha, "T999", Task.Status.COMPLETED));

        assertEquals("Task 'T999' was not found in the project.", error.getMessage());
    }

    @Test
    void testUnknownTaskInAllProjectsThrowsTaskNotFoundException() {
        assertThrows(TaskNotFoundException.class, () -> taskService.findProjectOfTask("T999"));
    }

    @Test
    void testTaskFromAnotherProjectIsNotFound() {
        Project iot = projectService.createHardwareProject("IoT Sensor Kit", "Sensors", 4000, 2, 4, "ESP32");
        taskService.addTask(iot, "Wire Board", Task.Status.PENDING, 2, null);

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTaskStatus(alpha, "TSK001", Task.Status.COMPLETED));
    }

    // removing tasks

    @Test
    void testRemoveTask() {
        taskService.addTask(alpha, "Setup DB", Task.Status.PENDING, 4, null);

        taskService.removeTask(alpha, "TSK001");

        assertEquals(0, alpha.getTaskCount());
        assertThrows(TaskNotFoundException.class, () -> taskService.findProjectOfTask("TSK001"));
    }
}
