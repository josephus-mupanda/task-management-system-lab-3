import models.Project;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.ProjectService;
import services.TaskService;
import utils.exceptions.InvalidInputException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskTest {

    private TaskService taskService;
    private Project alpha;

    @BeforeEach
    void setUp() {
        ProjectService projectService = new ProjectService();
        taskService = new TaskService(projectService);
        alpha = projectService.createSoftwareProject("P001", "Alpha Tracker", "API tracker", 15000, 5, 6, "Java");
    }

    // Feature 2: tasks in an ArrayList per project

    @Test
    void testAddTaskToProject() {
        Task task = taskService.addTask(alpha, "T001", "Implement API", Task.Status.IN_PROGRESS, 8, null);

        assertEquals("T001", task.getId());
        assertEquals(1, alpha.getTaskCount());
    }

    @Test
    void testUpdateTaskStatus() {
        taskService.addTask(alpha, "T001", "Setup DB", Task.Status.PENDING, 4, null);
        taskService.addTask(alpha, "T002", "Design UI", Task.Status.PENDING, 6, null);

        Task updated = taskService.updateTaskStatus(alpha, "T001", Task.Status.COMPLETED);

        assertEquals("Completed", updated.getStatus());
        assertEquals(50.0, alpha.calculateCompletionPercentage());
    }

    // US-2.1 and Workflow 2: the regex checks the format, then a Predicate checks that the ID is not used yet

    @Test
    void testTaskIdMustMatchPattern() {
        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> taskService.addTask(alpha, "TX01", "Write Unit Tests", Task.Status.PENDING, 2, null));

        assertEquals("Invalid Task ID format. Use pattern T### (e.g., T001)", error.getMessage());
    }

    @Test
    void testTaskIdMustBeUnique() {
        taskService.addTask(alpha, "T001", "Implement API", Task.Status.PENDING, 8, null);

        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> taskService.addTask(alpha, "T001", "Wire Board", Task.Status.PENDING, 2, null));

        assertEquals("Task ID 'T001' already exists.", error.getMessage());
    }
}
