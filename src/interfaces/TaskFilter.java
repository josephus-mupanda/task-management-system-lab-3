package interfaces;

import models.Task;

// a functional interface, so a filter can be passed as a lambda instead of writing a class for each rule
@FunctionalInterface
public interface TaskFilter {

    /** Returns true if the task passes the filter. */
    boolean matches(Task task);

    /** Keeps the tasks that have this status. */
    static TaskFilter byStatus(Task.Status status) {
        return task -> task.getStatusType() == status;
    }
}
