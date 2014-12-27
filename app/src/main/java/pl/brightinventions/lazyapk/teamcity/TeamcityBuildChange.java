package pl.brightinventions.lazyapk.teamcity;

import java.io.Serializable;
import java.util.Date;

class TeamcityBuildChange implements Serializable {
    String id;
    String version;
    Date date;
    String href;
    String comment;

    public void readDetailsFrom(TeamcityBuildChange teamcityBuildChange) {
        comment = teamcityBuildChange.comment;
    }

    @Override
    public String toString() {
        return "TeamcityBuildChange{" +
                "id='" + id + '\'' +
                ", href='" + href + '\'' +
                '}';
    }
}
