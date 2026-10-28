package models;

import interfaces.TaskFilter;
import utils.exceptions.EmptyProjectException;
import utils.exceptions.InvalidProjectDataException;
import utils.exceptions.TaskNotFoundException;

import java.util.ArrayList;
import java.util.List;

// abstract so each kind of project gives its own type and details, the code that prints projects does not change.
public abstract class Project {

    private final String id;
    private final String name;
    private final String description;
    private final double budget;
    private final int teamSize;
    private final int durationWeeks;
    private final List<Task> tasks = new ArrayList<>();

    /** Creates a project, throws InvalidProjectDataException if a value is not valid. */
    public Project(String id, String name, String description, double budget, int teamSize, int durationWeeks) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidProjectDataException("Project ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProjectDataException("Project name cannot be empty.");
        }
        if (budget <= 0) {
            throw new InvalidProjectDataException("Budget must be greater than 0.");
        }
        if (teamSize <= 0) {
            throw new InvalidProjectDataException("Team size must be greater than 0.");
        }
        if (durationWeeks <= 0) {
            throw new InvalidProjectDataException("Duration must be greater than 0.");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.budget = budget;
        this.teamSize = teamSize;
        this.durationWeeks = durationWeeks;
    }

    /** Returns the kind of project, ex: Software. */
    public abstract String getType();

    /** Returns the line that depends on the type, ex: Tech Stack: Java. */
    public abstract String getProjectDetails();

    /** Adds a task to the project. */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /** Returns the task with this id, throws TaskNotFoundException if it is not in this project. */
    public Task findTask(String taskId) {
        return tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    /** Returns true if the task is in this project. */
    public boolean hasTask(String taskId) {
        return tasks.stream().anyMatch(task -> task.getId().equals(taskId));
    }

    /** Removes the task, throws TaskNotFoundException if it is not here. */
    public void removeTask(String taskId) {
        if (!tasks.removeIf(task -> task.getId().equals(taskId))) {
            throw new TaskNotFoundException(taskId);
        }
    }

    /** Returns true if a task in this project already has this name (ignoring case). */
    public boolean hasTaskNamed(String taskName) {
        return tasks.stream().anyMatch(task -> task.getName().equalsIgnoreCase(taskName.trim()));
    }

    /** Returns a copy of the tasks so the list inside the project cannot be changed from outside. */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    /** Returns the tasks that pass the filter. */
    public List<Task> getTasks(TaskFilter filter) {
        return tasks.stream().filter(filter::matches).toList();
    }

    /** Returns the number of tasks. */
    public int getTaskCount() {
        return tasks.size();
    }

    /** Returns true if the project has at least one task. */
    public boolean hasTasks() {
        return !tasks.isEmpty();
    }

    /** Counts the tasks that have this status. */
    public int countByStatus(Task.Status status) {
        return getTasks(TaskFilter.byStatus(status)).size();
    }

    /** Returns the % of completed tasks rounded to 2 decimals, throws EmptyProjectException when there are no tasks. */
    public double calculateCompletionPercentage() {
        if (tasks.isEmpty()) {
            throw new EmptyProjectException(
                    "Project '" + name + "' has no tasks, so its completion cannot be calculated.");
        }
        long completed = tasks.stream().filter(Task::isCompleted).count();
        double percentage = (double) completed / tasks.size() * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }

    /** Same as calculateCompletionPercentage() but a project without tasks gives 0, so streams can sort and filter on it. */
    public double getCompletionRate() {
        return tasks.isEmpty() ? 0 : calculateCompletionPercentage();
    }

    /** Returns the id, ex: P001. */
    public String getId() {
        return id;
    }

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Returns the description. */
    public String getDescription() {
        return description;
    }

    /** Returns the budget. */
    public double getBudget() {
        return budget;
    }

    /** Returns the team size. */
    public int getTeamSize() {
        return teamSize;
    }

    /** Returns the duration in weeks. */
    public int getDurationWeeks() {
        return durationWeeks;
    }
}
