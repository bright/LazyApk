package pl.brightinventions.lazyapk.teamcity;

import java.util.ArrayList;

import pl.brightinventions.lazyapk.Linq;
import rx.functions.Func1;

class TeamcityProjectsResponse {
    int count;
    ArrayList<TeamcityProjectResponse> project;

    TeamcityProjectsResponse() {
        project = new ArrayList<>();
        count = 0;
    }

    public Iterable<TeamcityProjectResponse> getValidProjects() {
        return Linq.filter(project, new Func1<TeamcityProjectResponse, Boolean>() {
            @Override
            public Boolean call(TeamcityProjectResponse teamcityProject) {
                return !teamcityProject.isRoot();
            }
        });
    }
}
