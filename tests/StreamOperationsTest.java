import interfaces.TaskFilter;
import models.Project;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.ProjectService;
import services.StreamService;
import services.TaskService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamOperationsTest {

    private StreamService streamService;
    private Project alpha;

    @BeforeEach
    void setUp() {
        ProjectService projectService = new ProjectService();
        TaskService taskService = new TaskService(projectService);
        streamService = new StreamService(projectService);

        // alpha: 3 of 4 tasks completed (75%), iot: 1 of 2 completed (50%), so the expected numbers are known
        alpha = projectService.createSoftwareProject("P001", "Alpha Tracker", "", 15000, 5, 6, "Java");
        Project iot = projectService.createHardwareProject("P002", "IoT Sensor Kit", "", 4000, 2, 4, "ESP32");

        taskService.addTask(alpha, "T001", "Design DB", Task.Status.COMPLETED, 6, null);
        taskService.addTask(alpha, "T002", "Implement API", Task.Status.COMPLETED, 10, null);
        taskService.addTask(alpha, "T003", "Write Docs", Task.Status.COMPLETED, 3, null);
        taskService.addTask(alpha, "T004", "Write Tests", Task.Status.PENDING, 5, null);
        taskService.addTask(iot, "T005", "Build Prototype", Task.Status.COMPLETED, 12, null);
        taskService.addTask(iot, "T006", "Calibrate Sensor", Task.Status.IN_PROGRESS, 4, null);
    }

    // the example of the lab, written exactly as in the sheet

    @Test
    void testFilterCompletedTasksUsingStreams() {
        List<Task> tasks = List.of(
                new Task("T001", "Design UI", "Completed"),
                new Task("T002", "Write Tests", "Pending"));

        long completed = tasks.stream().filter(t -> t.getStatus().equals("Completed")).count();

        assertEquals(1, completed);
    }

    // Test Scenario 8: filter() to list completed tasks, map() to extract names, reduce() to compute total completion

    @Test
    void testFilterListsCompletedTasks() {
        List<Task> completed = streamService.filterTasks(TaskFilter.byStatus(Task.Status.COMPLETED));

        assertEquals(4, completed.size());
    }

    @Test
    void testMapExtractsTaskNames() {
        List<String> names = streamService.taskNames(TaskFilter.byStatus(Task.Status.PENDING));

        assertEquals(List.of("Write Tests"), names);
    }

    @Test
    void testReduceComputesTotalCompletion() {
        // 4 completed tasks out of 6
        assertEquals(66.67, streamService.overallCompletion());
    }

    // US-1.2: filter completed projects with a stream (the lab's example uses a limit of 70%)

    @Test
    void testFilterCompletedProjects() {
        List<Project> completed = streamService.projectsAboveCompletion(70);

        assertEquals(List.of(alpha), completed);
    }
}
