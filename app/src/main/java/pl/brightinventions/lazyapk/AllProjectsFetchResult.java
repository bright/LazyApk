package pl.brightinventions.lazyapk;

import java.util.ArrayList;
import java.util.List;

public class AllProjectsFetchResult {
    List<ProjectOverview> projectOverviews = new ArrayList<>();
    public void addFetchResult(ProjectSourceFetchResult fetchResult) {
        Linq.addAll(projectOverviews, fetchResult.getResults());
    }

    public Iterable<ProjectOverview> getAllProjects() {
        return projectOverviews;
    }
}
