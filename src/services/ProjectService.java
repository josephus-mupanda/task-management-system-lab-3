package services;

import models.HardwareProject;
import models.Project;
import models.SoftwareProject;
import utils.FunctionalUtils;
import utils.RegexValidator;
import utils.exceptions.InvalidProjectDataException;
import utils.exceptions.ProjectNotFoundException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// a HashMap so a project is found by its id in one step. It never prints, so it can be tested without a console.
public class ProjectService {

    private final Map<String, Project> projects = new HashMap<>();

    /** Creates a software project and saves it, throws InvalidProjectDataException if the id is not like P001 or a value is not valid. */
    public Project createSoftwareProject(String id, String name, String description, double budget,
                                         int teamSize, int durationWeeks, String techStack) {
        Project project = new SoftwareProject(id, name, description, budget, teamSize, durationWeeks, techStack);
        addProject(project);
        return project;
    }

    /** Creates a hardware project and saves it, throws InvalidProjectDataException if the id is not like P001 or a value is not valid. */
    public Project createHardwareProject(String id, String name, String description, double budget,
                                         int teamSize, int durationWeeks, String deviceType) {
        Project project = new HardwareProject(id, name, description, budget, teamSize, durationWeeks, deviceType);
        addProject(project);
        return project;
    }

    /** Saves a project, throws InvalidProjectDataException if its id or its name is already used. */
    public void addProject(Project project) {
        if (projects.containsKey(project.getId())) {
            throw new InvalidProjectDataException("Project ID '" + project.getId() + "' already exists.");
        }
        if (nameExists(project.getName())) {
            throw new InvalidProjectDataException("A project named '" + project.getName() + "' already exists.");
        }
        projects.put(project.getId(), project);
    }

    /** Removes a project and its tasks, throws ProjectNotFoundException if there is none with this id. */
    public Project removeProject(String input) {
        Project project = findProject(input);
        projects.remove(project.getId());
        return project;
    }

    /** Returns true if a project already has this name (ignoring case). */
    public boolean nameExists(String name) {
        return projects.values().stream().anyMatch(project -> project.getName().equalsIgnoreCase(name.trim()));
    }

    /** Finds a project by its id (P001), throws InvalidInputException if the format is wrong and ProjectNotFoundException if there is none. */
    public Project findProject(String input) {
        Project project = projects.get(RegexValidator.requireProjectId(input));
        if (project == null) {
            throw new ProjectNotFoundException(input.trim());
        }
        return project;
    }

    /** Returns all the projects sorted by id (a HashMap has no order of its own). */
    public List<Project> getProjects() {
        return projects.values().stream()
                .sorted(Comparator.comparing(Project::getId))
                .toList();
    }

    /** Returns the projects of one type, "Software" or "Hardware". */
    public List<Project> getByType(String type) {
        return FunctionalUtils.filter(getProjects(), project -> project.getType().equalsIgnoreCase(type));
    }

    /** Returns the projects with a budget between min and max. */
    public List<Project> getByBudgetRange(double min, double max) {
        return FunctionalUtils.filter(getProjects(),
                project -> project.getBudget() >= min && project.getBudget() <= max);
    }

    /** Returns true if a project already has this id. */
    public boolean idExists(String id) {
        return projects.containsKey(id.trim());
    }
}
