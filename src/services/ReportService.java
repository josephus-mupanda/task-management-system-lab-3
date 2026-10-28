package services;

import models.StatusReport;
import utils.FunctionalUtils;
import utils.exceptions.EmptyProjectException;

import java.util.List;

public class ReportService {

    private final ProjectService projectService;

    /** Uses the project service to get the projects. */
    public ReportService(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** Returns one report per project. */
    public List<StatusReport> generateReports() {
        return FunctionalUtils.map(projectService.getProjects(), StatusReport::new);
    }

    /** Returns the average completion of the projects that have tasks, throws EmptyProjectException if none has tasks. */
    public double calculateAverageCompletion(List<StatusReport> reports) {
        // projects without tasks are skipped, otherwise they would count as 0%
        List<StatusReport> counted = FunctionalUtils.filter(reports, StatusReport::hasTasks);

        if (counted.isEmpty()) {
            throw new EmptyProjectException("No project has tasks yet, so the average cannot be calculated.");
        }
        double total = counted.stream().map(StatusReport::getPercentage).reduce(0.0, Double::sum);
        return Math.round(total / counted.size() * 100.0) / 100.0;
    }
}
