package pl.brightinventions.lazyapk.teamcity;

import java.util.ArrayList;

class TeamcityBuildsResponse {
    int count;
    String href;
    ArrayList<TeamcityBuildResponse> build = new ArrayList<>();
}
