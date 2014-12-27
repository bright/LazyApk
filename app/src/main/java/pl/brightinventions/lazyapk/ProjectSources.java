package pl.brightinventions.lazyapk;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import pl.brightinventions.lazyapk.sources.ProjectSourceConfiguration;
import pl.brightinventions.lazyapk.teamcity.TeamcityCredentials;
import pl.brightinventions.lazyapk.teamcity.TeamcityProjectSource;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.FuncN;

@Singleton
public class ProjectSources {
    private ArrayList<ProjectSource> sources;

    @Inject
    public ProjectSources() {
        sources = new ArrayList<>();
    }

    public Observable<AllProjectsFetchResult> fetchProjects(){
        return Observable.zip(fetchEachProjectSource(), new FuncN<AllProjectsFetchResult>() {
            @Override
            public AllProjectsFetchResult call(Object... args) {
                AllProjectsFetchResult fetchResults = new AllProjectsFetchResult();
                for (Object resultItem : args) {
                    ProjectSourceFetchResult fetchResult = (ProjectSourceFetchResult) resultItem;
                    fetchResults.addFetchResult(fetchResult);
                }
                return fetchResults;
            }
        });
    }

    private Iterable<Observable<ProjectSourceFetchResult>> fetchEachProjectSource() {
        return Linq.map(sources, new Func1<ProjectSource, Observable<ProjectSourceFetchResult>>() {
            @Override
            public Observable<ProjectSourceFetchResult> call(ProjectSource projectSource) {
                return projectSource.fetchProjects();
            }
        });
    }

    public ProjectSource getSourceFor(final ProjectOverview projectOverview) {
        return Linq.findFirst(sources, new Func1<ProjectSource, Boolean>() {
            @Override
            public Boolean call(ProjectSource source) {
                return source.hasProject(projectOverview);
            }
        }, ProjectSource.EMPTY);
    }

    public void removeAll() {
        sources = new ArrayList<>();
    }

    public void addProjectSource(ProjectSource projectSource) {
        sources.add(projectSource);
    }
}
