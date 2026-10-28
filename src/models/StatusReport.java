package models;

// keeps a copy of the numbers, so the report does not change if the tasks change after it is made
public class StatusReport {

    private final String projectId;
    private final String projectName;
    private final int totalTasks;
    private final int completedTasks;
    private final int inProgressTasks;
    private final int pendingTasks;
    private final double percentage;

    /** Takes the numbers from the project, a project without tasks gets 0 and hasTasks() returns false. */
    public StatusReport(Project project) {
        this.projectId = project.getId();
        this.projectName = project.getName();
        this.totalTasks = project.getTaskCount();
        this.completedTasks = project.countByStatus(Task.Status.COMPLETED);
        this.inProgressTasks = project.countByStatus(Task.Status.IN_PROGRESS);
        this.pendingTasks = project.countByStatus(Task.Status.PENDING);
        this.percentage = project.hasTasks() ? project.calculateCompletionPercentage() : 0;
    }

    /** Returns true if the project had at least one task. */
    public boolean hasTasks() {
        return totalTasks > 0;
    }

    /** Returns the project id. */
    public String getProjectId() {
        return projectId;
    }

    /** Returns the project name. */
    public String getProjectName() {
        return projectName;
    }

    /** Returns the number of tasks. */
    public int getTotalTasks() {
        return totalTasks;
    }

    /** Returns the number of completed tasks. */
    public int getCompletedTasks() {
        return completedTasks;
    }

    /** Returns the number of tasks in progress. */
    public int getInProgressTasks() {
        return inProgressTasks;
    }

    /** Returns the number of pending tasks. */
    public int getPendingTasks() {
        return pendingTasks;
    }

    /** Returns the completion percentage. */
    public double getPercentage() {
        return percentage;
    }
}
