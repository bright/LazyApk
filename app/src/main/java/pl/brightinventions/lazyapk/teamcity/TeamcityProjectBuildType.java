package pl.brightinventions.lazyapk.teamcity;

import java.io.Serializable;

class TeamcityProjectBuildType implements Serializable {
    String id;
    String name;
    String projectName;
    String projectId;
    String href;
    TeamcityProjectResponse project;
}
