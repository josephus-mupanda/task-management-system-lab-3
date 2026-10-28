package models;

import interfaces.Completable;
import utils.ValidationUtils;
import utils.exceptions.InvalidInputException;

public class Task implements Completable {

    public enum Status {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        /** Turns text like "in progress" into a status, throws InvalidInputException if it matches none. */
        public static Status fromText(String text) {
            for (Status status : values()) {
                if (status.label.equalsIgnoreCase(text.trim())) {
                    return status;
                }
            }
            throw new InvalidInputException("Invalid status. Please choose from [Pending, In Progress, Completed].");
        }

        /** Returns the label shown to the user. */
        @Override
        public String toString() {
            return label;
        }
    }

    private final String id;
    private final String name;
    private final int hours;
    private Status status;
    private User assignedTo;

    /** Creates a task, throws InvalidInputException if the id or the name is empty or the hours are not positive. */
    public Task(String id, String name, Status status, int hours) {
        this.id = ValidationUtils.requireText(id, "Task ID");
        this.name = ValidationUtils.requireText(name, "Task name");
        this.status = status;
        this.hours = ValidationUtils.requirePositive(hours, "Hours");
    }

    /** Short version used in the lab's JUnit example: the status is given as text and the estimate is 1 hour. */
    public Task(String id, String name, String status) {
        this(id, name, Status.fromText(status), 1);
    }

    /** Returns true if the status is Completed. */
    @Override
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    /** Returns the id, ex: T001. */
    public String getId() {
        return id;
    }

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Returns the estimated hours. */
    public int getHours() {
        return hours;
    }

    /** Returns the status as text, ex: "Completed", so it can be compared with getStatus().equals("Completed"). */
    public String getStatus() {
        return status.toString();
    }

    /** Returns the status as a Task.Status value, used when the code needs the next status or compares statuses. */
    public Status getStatusType() {
        return status;
    }

    /** Changes the status. */
    public void setStatus(Status status) {
        this.status = status;
    }

    /** Returns the assigned user, or null if nobody is assigned. */
    public User getAssignedTo() {
        return assignedTo;
    }

    /** Assigns the task to a user, null means nobody. */
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
}
