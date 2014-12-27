package pl.brightinventions.lazyapk.teamcity;

import android.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class TeamcityBuildArtifactCollection implements ObservableCollection<TeamcityBuildArtifactsFile> {
    private static final Logger LOG = LoggerFactory.getLogger(TeamcityBuildArtifactCollection.class.getSimpleName());
    private final TeamcityProjectSource projectSource;
    private final TeamcityBuildResponse buildResponse;
    private final ArrayList<TeamcityBuildArtifactsFile> buildArtifactsFiles;
    private final PublishSubject<Pair<TeamcityBuildArtifactsFile, Integer>> itemInsertedAt = PublishSubject.create();
    private PublishSubject<Pair<Integer, Integer>> itemsAtRangeRemoved = PublishSubject.create();

    public TeamcityBuildArtifactCollection(TeamcityProjectSource projectSource, TeamcityBuildResponse buildResponse) {
        this.projectSource = projectSource;
        this.buildResponse = buildResponse;
        this.buildArtifactsFiles = new ArrayList<>();
    }

    @Override
    public int size() {
        synchronized (this.buildArtifactsFiles) {
            return this.buildArtifactsFiles.size();
        }
    }

    @Override
    public TeamcityBuildArtifactsFile get(int position) {
        synchronized (this.buildArtifactsFiles) {
            return this.buildArtifactsFiles.get(position);
        }
    }

    @Override
    public Observable<Pair<TeamcityBuildArtifactsFile, Integer>> itemInsertedAt() {
        return itemInsertedAt;
    }

    @Override
    public Observable<Pair<Integer, Integer>> itemsAtRangeRemoved() {
        return itemsAtRangeRemoved;
    }

    @Override
    public Iterator<TeamcityBuildArtifactsFile> iterator() {
        synchronized (this.buildArtifactsFiles) {
            return new ArrayList<>(buildArtifactsFiles).iterator();
        }
    }

    public Observable<?> fetchBuildArtifacts() {
        TeamcityBuildArtifactsFiles artifacts = buildResponse.artifacts;
        TeamcityBuildArtifactsFile file = new TeamcityBuildArtifactsFile();
        file.href = artifacts.href;
        file.name = "_root";
        file.children = artifacts;
        return fetchHierarchyForDirectory(file);
    }

    private Observable<TeamcityBuildArtifactsFile> fetchHierarchyForDirectory(final TeamcityBuildArtifactsFile parent) {
        LOG.trace("Fetch hierarchy {} (children: {})", parent, parent.children);
        if(parent.isProbablyDirectory() && parent.children != null) {
            return projectSource.fetchArtifactsChildren(parent)
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Func1<TeamcityBuildArtifactsFile, Observable<TeamcityBuildArtifactsFile>>() {
                        @Override
                        public Observable<TeamcityBuildArtifactsFile> call(TeamcityBuildArtifactsFile apkFileReference) {
                            insertAsChildOf(parent, apkFileReference);
                            return fetchHierarchyForDirectory(apkFileReference);
                        }
                    });
        } else {
            return Observable.empty();
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void insertAsChildOf(TeamcityBuildArtifactsFile parent, TeamcityBuildArtifactsFile child) {
        int insertedPosition;
        LOG.trace("Insert child {} from parent {}", child, parent);
        child.nestLevel = parent.nestLevel + 1;
        synchronized (this.buildArtifactsFiles){
            int parentIndex = buildArtifactsFiles.indexOf(parent);
            if(parentIndex == -1){
                buildArtifactsFiles.add(child);
                insertedPosition = 0;
            } else {
                buildArtifactsFiles.add(parentIndex + 1, child);
                insertedPosition = parentIndex + 1;
            }
        }
        itemInsertedAt.onNext(Pair.create(child, insertedPosition));
    }

    public void clear() {
        int size = buildArtifactsFiles.size();
        buildArtifactsFiles.clear();
        itemsAtRangeRemoved.onNext(Pair.create(0, size));

    }
}
