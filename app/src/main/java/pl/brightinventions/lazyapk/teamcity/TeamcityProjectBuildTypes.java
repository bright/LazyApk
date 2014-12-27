package pl.brightinventions.lazyapk.teamcity;

import java.io.Serializable;
import java.util.ArrayList;

class TeamcityProjectBuildTypes implements Serializable {
    int count;
    ArrayList<TeamcityProjectBuildType> buildType = new ArrayList<>();
}
