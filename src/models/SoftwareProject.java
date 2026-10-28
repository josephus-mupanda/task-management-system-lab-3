package models;

public class SoftwareProject extends Project {

    private final String techStack;

    /** Creates a software project, throws InvalidProjectDataException if a value is not valid. */
    public SoftwareProject(String id, String name, String description, double budget,
                           int teamSize, int durationWeeks, String techStack) {
        super(id, name, description, budget, teamSize, durationWeeks);
        this.techStack = techStack;
    }

    /** Short version used in the lab's JUnit example: no description, 1 week and no tech stack yet. */
    public SoftwareProject(String id, String name, int teamSize, double budget) {
        this(id, name, "", budget, teamSize, 1, "Not specified");
    }

    /** Returns the tech stack. */
    public String getTechStack() {
        return techStack;
    }

    /** Returns "Software". */
    @Override
    public String getType() {
        return "Software";
    }

    /** Returns the tech stack line shown in the details. */
    @Override
    public String getProjectDetails() {
        return "Tech Stack: " + techStack;
    }
}
