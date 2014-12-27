package pl.brightinventions.lazyapk.teamcity;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Date;

class TeamcityBuildArtifactsFile implements Serializable {
    String href;
    String name;
    int size;
    transient int nestLevel;
    Date modificationTime;
    TeamcityBuildArtifactsFiles children;
    TeamcityContentFile content;

    @Override
    public String toString() {
        return "TeamcityBuildArtifactsFile{" +
                "name='" + name + '\'' +
                ", href='" + href + '\'' +
                '}';
    }

    public boolean isProbablyDirectory() {
        return size == 0 || content == null;
    }

    public boolean isContent(){
        return content != null && !TextUtils.isEmpty(content.href);
    }
}
