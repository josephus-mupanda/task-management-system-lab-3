package utils;

import models.HardwareProject;
import models.Project;
import models.SoftwareProject;
import models.Task;
import models.User;
import utils.exceptions.DataFileException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// a small .json-like format written by hand, because the lab does not use a JSON library.
// Loading uses Files.readAllLines() (Feature 3), saving uses Files.writeString() and Files.readString() (US-3.1),
// and countProjects() uses Files.lines() with try-with-resources (Feature 3 functional reading).
public class FileUtils {

    /** Where the program keeps its data. */
    public static final Path DATA_FILE = Paths.get("data", "projects_data.json");

    // one task: {"id": "T001", "name": "Design DB", "status": "Completed", ...}
    private static final Pattern TASK_PATTERN = Pattern.compile("\\{[^{}]*\"id\"[^{}]*\\}");

    // one field: "name": "Alpha Tracker" or "budget": 8000.0
    private static final Pattern FIELD_PATTERN = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"([^\"]*)\"|[^,\\s}]+)");

    // ---------- saving ----------

    /**
     * Writes all the projects to the file with Files.writeString(), then reads the file back to confirm the write.
     * Throws DataFileException if the file cannot be written or does not contain what was written.
     */
    public static void saveProjects(List<Project> projects, Path path) {
        String json = projects.stream()
                .map(FileUtils::projectToJson)
                .collect(Collectors.joining(",\n", "[\n", "\n]\n"));
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, json);

            // the lab asks to confirm the write (Workflow 3), so the file is read back before saying it worked
            String saved = Files.readString(path);
            if (!saved.equals(json) || countProjects(path) != projects.size()) {
                throw new DataFileException("The saved file does not contain the data that was written");
            }
        } catch (IOException e) {
            throw new DataFileException("Unable to write the file: " + e.getMessage());
        }
    }

    /** Counts the projects in the file, throws DataFileException if the file cannot be read. */
    public static long countProjects(Path path) {
        // Files.lines keeps the file open while we read, try-with-resources closes it at the end
        try (Stream<String> lines = Files.lines(path)) {
            return lines.filter(line -> line.contains("\"projectId\"")).count();
        } catch (IOException | UncheckedIOException e) {
            throw new DataFileException("Unable to read the file");
        }
    }

    private static String projectToJson(Project project) {
        String detail;
        if (project instanceof SoftwareProject) {
            detail = "\"techStack\": " + quote(((SoftwareProject) project).getTechStack());
        } else {
            detail = "\"deviceType\": " + quote(((HardwareProject) project).getDeviceType());
        }

        String tasks = project.getTasks().stream()
                .map(FileUtils::taskToJson)
                .collect(Collectors.joining(",\n"));

        return "  {\n"
                + "    \"projectId\": " + quote(project.getId()) + ",\n"
                + "    \"name\": " + quote(project.getName()) + ",\n"
                + "    \"type\": " + quote(project.getType()) + ",\n"
                + "    \"description\": " + quote(project.getDescription()) + ",\n"
                + "    \"budget\": " + plainNumber(project.getBudget()) + ",\n"
                + "    \"teamSize\": " + project.getTeamSize() + ",\n"
                + "    \"durationWeeks\": " + project.getDurationWeeks() + ",\n"
                + "    " + detail + ",\n"
                + "    \"tasks\": [\n" + tasks + "\n    ]\n"
                + "  }";
    }

    private static String taskToJson(Task task) {
        String assignee = task.getAssignedTo() == null ? "null" : quote(task.getAssignedTo().getId());
        return "      {\"id\": " + quote(task.getId())
                + ", \"name\": " + quote(task.getName())
                + ", \"status\": " + quote(task.getStatus())
                + ", \"hours\": " + task.getHours()
                + ", \"assignedTo\": " + assignee + "}";
    }

    // a double of 10 million or more becomes "1.0E7" as text, which the loader refuses, so the budget is written with all its digits
    private static String plainNumber(double value) {
        return BigDecimal.valueOf(value).toPlainString();
    }

    // the file uses " around texts, so a " inside a name is saved as '
    private static String quote(String text) {
        return "\"" + text.replace("\"", "'") + "\"";
    }

    // ---------- loading ----------

    /**
     * Reads the projects from the file. The users are used to find who a task is assigned to.
     * Throws DataFileException if the file is missing, cannot be read or is not in the expected format,
     * and InvalidInputException if a value is not valid (for example a task ID that is not like T001).
     */
    public static List<Project> loadProjects(Path path, List<User> users) {
        if (!Files.exists(path)) {
            throw new DataFileException("File not found");
        }

        // the lab asks to auto-load the file on startup with Files.readAllLines()
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new DataFileException("Unable to read the file");
        }
        String text = lines.stream()
                .map(String::trim)
                .collect(Collectors.joining(" "));

        List<Project> projects = new ArrayList<>();
        if (text.isBlank()) {
            return projects;
        }
        if (!text.startsWith("[") || !text.endsWith("]")) {
            throw new DataFileException("Malformed data: the file must be a list [ ... ]");
        }

        // every project starts with {"projectId", so the text is cut there: one piece per project
        String[] pieces = text.split("\\{\\s*\"projectId\"");
        if (pieces.length == 1 && !text.replace(" ", "").equals("[]")) {
            throw new DataFileException("Malformed data: no project found");
        }
        for (int i = 1; i < pieces.length; i++) {
            projects.add(readProject("{\"projectId\"" + pieces[i], users));
        }
        return projects;
    }

    private static Project readProject(String text, List<User> users) {
        // the tasks are read first, then removed so only the fields of the project are left
        List<Task> tasks = new ArrayList<>();
        Matcher taskMatcher = TASK_PATTERN.matcher(text);
        while (taskMatcher.find()) {
            tasks.add(readTask(taskMatcher.group(), users));
        }
        Map<String, String> fields = readFields(TASK_PATTERN.matcher(text).replaceAll(""));

        String id = requireField(fields, "projectId");
        String name = requireField(fields, "name");
        String type = requireField(fields, "type");
        String description = requireField(fields, "description");
        double budget = ValidationUtils.parseDouble(requireField(fields, "budget"), "Budget");
        int teamSize = ValidationUtils.parseInt(requireField(fields, "teamSize"), "Team size");
        int duration = ValidationUtils.parseInt(requireField(fields, "durationWeeks"), "Duration");

        Project project;
        if (type.equals("Software")) {
            String techStack = requireField(fields, "techStack");
            project = new SoftwareProject(id, name, description, budget, teamSize, duration, techStack);
        } else if (type.equals("Hardware")) {
            String deviceType = requireField(fields, "deviceType");
            project = new HardwareProject(id, name, description, budget, teamSize, duration, deviceType);
        } else {
            throw new DataFileException("Malformed data: unknown project type '" + type + "'");
        }

        tasks.forEach(project::addTask);
        return project;
    }

    private static Task readTask(String text, List<User> users) {
        Map<String, String> fields = readFields(text);

        Task task = new Task(requireField(fields, "id"), requireField(fields, "name"),
                Task.Status.fromText(requireField(fields, "status")),
                ValidationUtils.parseInt(requireField(fields, "hours"), "Hours"));

        // users are not saved, so a user that does not exist any more leaves the task unassigned instead of failing
        String userId = requireField(fields, "assignedTo");
        User assignee = users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
        task.setAssignedTo(assignee);
        return task;
    }

    // a map, so a field is read by its name whatever its place in the text
    private static Map<String, String> readFields(String text) {
        Map<String, String> fields = new HashMap<>();
        Matcher matcher = FIELD_PATTERN.matcher(text);
        while (matcher.find()) {
            String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(2);
            fields.put(matcher.group(1), value);
        }
        return fields;
    }

    private static String requireField(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (value == null) {
            throw new DataFileException("Malformed data: field '" + key + "' is missing");
        }
        return value;
    }
}
