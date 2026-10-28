import models.Project;
import models.SoftwareProject;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.ProjectService;
import utils.exceptions.EmptyProjectException;
import utils.exceptions.InvalidProjectDataException;
import utils.exceptions.ProjectNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectTest {

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService();
    }

    // Lab 2: the completion percentage, the existing tests must still pass

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

    // US-2.2: project IDs must match P\d{3}

    @Test
    void testProjectIdMustMatchPattern() {
        InvalidProjectDataException error = assertThrows(InvalidProjectDataException.class,
                () -> projectService.createSoftwareProject("PRJ001", "Alpha Tracker", "", 15000, 5, 6, "Java"));

        assertEquals("Invalid Project ID format. Use pattern P### (e.g., P001)", error.getMessage());
    }

    @Test
    void testDuplicateProjectIdIsRejected() {
        projectService.createSoftwareProject("P001", "Alpha Tracker", "", 15000, 5, 6, "Java");

        assertThrows(InvalidProjectDataException.class,
                () -> projectService.createSoftwareProject("P001", "Another Project", "", 3000, 2, 4, "Java"));
    }

    // Feature 1: projects in a HashMap, added and removed without size limits

    @Test
    void testRemoveProject() {
        projectService.createSoftwareProject("P001", "Alpha Tracker", "", 15000, 5, 6, "Java");

        projectService.removeProject("P001");

        assertEquals(0, projectService.getProjects().size());
        assertThrows(ProjectNotFoundException.class, () -> projectService.findProject("P001"));
    }

    @Test
    void testProjectsComeBackSortedById() {
        // added in reverse order: a HashMap has no order, so getProjects() sorts them with a stream
        projectService.createSoftwareProject("P003", "Gamma", "", 1000, 1, 1, "Java");
        projectService.createSoftwareProject("P002", "Beta", "", 1000, 1, 1, "Java");
        projectService.createSoftwareProject("P001", "Alpha", "", 1000, 1, 1, "Java");

        List<Project> projects = projectService.getProjects();

        assertEquals("P001", projects.get(0).getId());
        assertEquals("P002", projects.get(1).getId());
        assertEquals("P003", projects.get(2).getId());
    }
}
