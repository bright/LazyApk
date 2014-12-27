package pl.brightinventions.lazyapk.teamcity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import pl.brightinventions.lazyapk.ProjectApkSource;
import pl.brightinventions.lazyapk.ProjectOverview;
import pl.brightinventions.lazyapk.ProjectSource;
import pl.brightinventions.lazyapk.R;
import rx.Observable;

class TeamcityBuildResponse implements ProjectApkSource {
    String id;
    String buildTypeId;
    String number;
    String status;
    String statusText;
    String state;
    String branchName;
    String href;
    Date queuedDate;
    Date startDate;
    Date finishDate;
    TeamcityBuildChanges changes = new TeamcityBuildChanges();
    TeamcityBuildLastChanges lastChanges = new TeamcityBuildLastChanges();
    TeamcityBuildArtifactsFiles artifacts = new TeamcityBuildArtifactsFiles();
    private transient Observable<ProjectApkSource> alreadyLoading;
    private TeamcityProjectResponse projectOverView;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public MasterDetailsViewHolderOf<ProjectApkSource> createListItemViewHolder(LayoutInflater layoutInflater, ViewGroup parent, ProjectSource projectSource) {
        return new TeamcityApkSourceViewHolder(layoutInflater.inflate(R.layout.teamcity_build_details_info, parent, false), projectSource);
    }

    @Override
    public Observable<ProjectApkSource> fetchDetailsCachedOrNewWithDelay(ProjectSource projectSource, int amount, TimeUnit unit) {
        if(areDetailsLoaded()){
            return Observable.just((ProjectApkSource)this);
        } else {
            if(alreadyLoading == null){
                alreadyLoading = projectSource.fetchApkSourceDetails(this)
                    .delaySubscription(amount, unit);
            }
            return alreadyLoading;
        }
    }

    @Override
    public Intent newDetailsIntent(Context activity) {
        return TeamcityBuildDetailsActivity.newIntent(this, activity);
    }

    private boolean areDetailsLoaded() {
        return startDate != null;
    }

    public void readDetailsFrom(TeamcityBuildResponse buildDetails) {
        statusText = buildDetails.statusText;
        startDate = buildDetails.startDate;
        queuedDate = buildDetails.queuedDate;
        finishDate = buildDetails.finishDate;
        changes = buildDetails.changes;
        lastChanges = buildDetails.lastChanges;
        artifacts = buildDetails.artifacts;
    }

    public boolean hasLastChange() {
        return lastChanges != null && lastChanges.count > 0;
    }

    public void updateLastChangesDetails(TeamcityBuildChange change) {
        lastChanges.updateFirst(change);
    }

    @Override
    public String toString() {
        return "TeamcityBuildResponse{" +
                "id='" + id + '\'' +
                ", href='" + href + '\'' +
                '}';
    }

    public TeamcityProjectResponse getProjectOverView() {
        return projectOverView;
    }

    public void setProjectOverView(TeamcityProjectResponse projectOverView) {
        if(projectOverView == null){
            throw new IllegalArgumentException("projectOverview must not be null");
        }
        this.projectOverView = projectOverView;
    }

    public String lastChangeComment() {
        if(hasLastChange()){
            return lastChanges.first().comment;
        }
        return null;
    }
}
