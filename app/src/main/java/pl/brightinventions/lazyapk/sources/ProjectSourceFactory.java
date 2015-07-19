package pl.brightinventions.lazyapk.sources;

import javax.inject.Inject;

import pl.brightinventions.lazyapk.ProjectSource;
import pl.brightinventions.lazyapk.teamcity.TeamcityCredentials;
import pl.brightinventions.lazyapk.teamcity.TeamcityProjectSource;

public class ProjectSourceFactory {

    @Inject
    public ProjectSourceFactory() {
    }

    public ProjectSource create(ProjectSourceConfiguration config) {
        TeamcityCredentials credentials = new TeamcityCredentials(config.getUserName(), config.getPassword());
        TeamcityProjectSource projectSource = new TeamcityProjectSource(config.getAddress(), credentials);
        return projectSource;
    }
}
