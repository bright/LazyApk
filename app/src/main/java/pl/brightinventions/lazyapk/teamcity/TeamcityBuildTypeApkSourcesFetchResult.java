package pl.brightinventions.lazyapk.teamcity;

import pl.brightinventions.lazyapk.FetchListResult;
import pl.brightinventions.lazyapk.ProjectApkSource;
import pl.brightinventions.lazyapk.ProjectOverview;

class TeamcityBuildTypeApkSourcesFetchResult extends FetchListResult<ProjectApkSource> {
    private final TeamcityProjectBuildType buildType;

    public TeamcityBuildTypeApkSourcesFetchResult(TeamcityProjectBuildType buildType) {
        this.buildType = buildType;
    }

    @Override
    public void addResults(Iterable<? extends ProjectApkSource> resultsToAdd) {
        for(ProjectApkSource teamcityBuildResponse:resultsToAdd){
            TeamcityBuildResponse buildResponse = (TeamcityBuildResponse) teamcityBuildResponse;
            buildResponse.setProjectOverView(buildType.project);
        }
        super.addResults(resultsToAdd);
    }
}
