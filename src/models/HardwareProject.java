package models;

public class HardwareProject extends Project {

    private final String deviceType;

    /** Creates a hardware project, throws InvalidProjectDataException if a value is not valid. */
    public HardwareProject(String id, String name, String description, double budget,
                           int teamSize, int durationWeeks, String deviceType) {
        super(id, name, description, budget, teamSize, durationWeeks);
        this.deviceType = deviceType;
    }

    /** Returns the device type. */
    public String getDeviceType() {
        return deviceType;
    }

    /** Returns "Hardware". */
    @Override
    public String getType() {
        return "Hardware";
    }

    /** Returns the device type line shown in the details. */
    @Override
    public String getProjectDetails() {
        return "Device Type: " + deviceType;
    }
}
