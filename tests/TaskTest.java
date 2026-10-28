import models.Project;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.ConcurrencyService;
import services.ProjectService;
import services.TaskService;
import utils.exceptions.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {

    private TaskService taskService;
    private ConcurrencyService concurrencyService;
    private Project alpha;

    @BeforeEach
    void setUp() {
        ProjectService projectService = new ProjectService();
        taskService = new TaskService(projectService);
        concurrencyService = new ConcurrencyService();
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

    // US-4.1 and US-4.2: concurrent updates with threads and with a parallel stream

    // the tasks are pending, so each update has a next status to move to
    private List<Task> addPendingTasks(int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            tasks.add(taskService.addTask(alpha, String.format("T%03d", i), "Task " + i, Task.Status.PENDING, 1, null));
        }
        return tasks;
    }

    @Test
    void testThreadsUpdateEveryTask() throws InterruptedException {
        List<Task> tasks = addPendingTasks(3);

        concurrencyService.updateWithThreads(tasks);

        assertTrue(tasks.stream().allMatch(task -> task.getStatus().equals("In Progress")));
    }

    @Test
    void testParallelStreamUpdatesEveryTask() {
        List<Task> tasks = addPendingTasks(10);

        concurrencyService.updateWithParallelStream(tasks);

        assertTrue(tasks.stream().allMatch(task -> task.getStatus().equals("In Progress")));
    }

    @Test
    void testCountIsCorrectWithManyThreads() throws InterruptedException {
        // without synchronized in countUpdate(), two threads could write the same number and an update would be lost
        List<Task> tasks = addPendingTasks(200);

        concurrencyService.updateWithThreads(tasks);

        assertEquals(200, concurrencyService.getUpdatedCount());
    }
}
