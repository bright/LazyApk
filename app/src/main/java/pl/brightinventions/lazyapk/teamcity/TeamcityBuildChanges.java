package pl.brightinventions.lazyapk.teamcity;

import java.io.Serializable;
import java.util.ArrayList;

class TeamcityBuildChanges implements Serializable {
    String href;
    int count;
    ArrayList<TeamcityBuildChange> change = new ArrayList<>();
}
