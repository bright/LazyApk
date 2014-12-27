package pl.brightinventions.lazyapk.teamcity;

import pl.brightinventions.lazyapk.ProjectOverview;

class TeamcityProjectResponse implements ProjectOverview {
    String id;
    String name;
    String description;
    String href;
    String webUrl;
    TeamcityProjectBuildTypes buildTypes = new TeamcityProjectBuildTypes();

    public Boolean isRoot() {
        return "_Root".equals(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Iterable<TeamcityProjectBuildType> getBuildTypes() {
        for(TeamcityProjectBuildType buildType:buildTypes.buildType){
            buildType.project = this;
        }
        return buildTypes.buildType;
    }
}
