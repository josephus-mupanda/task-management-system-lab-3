package services;

import models.Project;
import models.Task;
import models.User;
import utils.FunctionalUtils;
import utils.RegexValidator;
import utils.exceptions.InvalidInputException;
import utils.exceptions.TaskNotFoundException;

import java.util.function.Predicate;

// task ids are typed by the user, so uniqueness is checked in every project, not only in one.
public class TaskService {

    private final ProjectService projectService;

    /** Uses the project service to look for tasks in every project. */
    public TaskService(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Creates a task and adds it to the project.
     * Throws InvalidInputException if the id is not like T001, if another task already has it,
     * or if the name is already used in this project.
     */
    public Task addTask(Project project, String id, String name, Task.Status status, int hours, User assignee) {
        // the format is checked first, so "TX01" gets the format error and not "already exists"
        String taskId = RegexValidator.requireTaskId(id);
        Predicate<String> isUnique = this::isIdAvailable;
        FunctionalUtils.validate(taskId, isUnique, "Task ID '" + taskId + "' already exists.");

        if (project.hasTaskNamed(name)) {
            throw new InvalidInputException("A task named '" + name.trim() + "' already exists in this project.");
        }
        Task task = new Task(taskId, name, status, hours);
        task.setAssignedTo(assignee);
        project.addTask(task);
        return task;
    }

    /** Returns true if no project has a task with this id. */
    public boolean isIdAvailable(String id) {
        return projectService.getProjects().stream().noneMatch(project -> project.hasTask(id.trim()));
    }

    /** Returns the project that has this task, throws TaskNotFoundException if no project has it. */
    public Project findProjectOfTask(String input) {
        String taskId = RegexValidator.requireTaskId(input);
        return projectService.getProjects().stream()
                .filter(project -> project.hasTask(taskId))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    /** Finds a task inside one project, throws TaskNotFoundException if it is not there. */
    public Task findTask(Project project, String input) {
        return project.findTask(RegexValidator.requireTaskId(input));
    }

    /** Changes the status of a task, throws TaskNotFoundException if it is not in the project. */
    public Task updateTaskStatus(Project project, String input, Task.Status status) {
        Task task = findTask(project, input);
        task.setStatus(status);
        return task;
    }

    /** Removes a task from the project, throws TaskNotFoundException if it is not there. */
    public Task removeTask(Project project, String input) {
        Task task = findTask(project, input);
        project.removeTask(task.getId());
        return task;
    }
}
