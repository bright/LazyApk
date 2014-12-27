package pl.brightinventions.lazyapk.teamcity;

import java.io.Serializable;
import java.util.ArrayList;

class TeamcityBuildLastChanges implements Serializable{
    int count;
    ArrayList<TeamcityBuildChange> change = new ArrayList<>();

    public TeamcityBuildChange first() {
        return change.get(0);
    }

    public void updateFirst(TeamcityBuildChange newChange) {
        first().readDetailsFrom(newChange);
    }
}
