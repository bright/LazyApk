package pl.brightinventions.lazyapk.teamcity;

import java.io.Serializable;
import java.util.ArrayList;

class TeamcityBuildArtifactsFiles implements Serializable  {
    String href;
    ArrayList<TeamcityBuildArtifactsFile> files = new ArrayList<>();
    ArrayList<TeamcityBuildArtifactsFile> file = new ArrayList<>();

    public Iterable<TeamcityBuildArtifactsFile> getChildFiles() {
        if (files == null || files.size() == 0) {
            return file;
        }
        return files;
    }
}
