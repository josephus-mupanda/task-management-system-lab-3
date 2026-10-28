package services;

import interfaces.TaskFilter;
import models.Project;
import models.Task;
import utils.exceptions.EmptyProjectException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// the stream queries are kept here, so ProjectService only stores the projects (single responsibility)
public class StreamService {

    private final ProjectService projectService;

    /** Uses the project service to get the projects. */
    public StreamService(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** Returns the projects with a completion above the limit, the most complete first. */
    public List<Project> projectsAboveCompletion(double limit) {
        return projectService.getProjects().stream()
                .filter(project -> project.getCompletionRate() > limit)
                .sorted(Comparator.comparing(Project::getCompletionRate).reversed())
                .collect(Collectors.toList());
    }

    /** Returns the tasks of every project. */
    public List<Task> allTasks() {
        return projectService.getProjects().stream()
                .flatMap(project -> project.getTasks().stream())
                .collect(Collectors.toList());
    }

    /** Returns the tasks of every project that pass the filter. */
    public List<Task> filterTasks(TaskFilter filter) {
        return allTasks().stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }

    /** Returns the names of the tasks that pass the filter. */
    public List<String> taskNames(TaskFilter filter) {
        return filterTasks(filter).stream()
                .map(Task::getName)
                .collect(Collectors.toList());
    }

    /** Returns the % of completed tasks among all the tasks, throws EmptyProjectException if there are no tasks. */
    public double overallCompletion() {
        List<Task> tasks = allTasks();
        if (tasks.isEmpty()) {
            throw new EmptyProjectException("There are no tasks yet, so the completion cannot be calculated.");
        }
        // mapped to 1 or 0 because reduce() adds numbers, not tasks
        int completed = tasks.stream()
                .map(task -> task.isCompleted() ? 1 : 0)
                .reduce(0, Integer::sum);
        double percentage = (double) completed / tasks.size() * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }
}
