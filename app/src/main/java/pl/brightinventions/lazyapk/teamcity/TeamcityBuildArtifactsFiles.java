package pl.brightinventions.lazyapk.teamcity;

import java.io.Serializable;
import java.util.ArrayList;

class TeamcityBuildArtifactsFiles implements Serializable  {
    String href;
    ArrayList<TeamcityBuildArtifactsFile> files = new ArrayList<>();
}
