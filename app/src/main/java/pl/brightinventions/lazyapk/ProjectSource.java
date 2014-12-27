package pl.brightinventions.lazyapk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

public interface ProjectSource {
    ProjectSource EMPTY = new EmptyProjectSource();

    Observable<ProjectSourceFetchResult> fetchProjects();
    Observable<ProjectApkSourceFetchResult> fetchApkSources(ProjectOverview projectOverview);
    Observable<ProjectApkSource> fetchApkSourceDetails(ProjectApkSource apkSourceAt);

    boolean hasProject(ProjectOverview projectOverview);

    static class EmptyProjectSource implements ProjectSource {
        private static final Logger LOG = LoggerFactory.getLogger(EmptyProjectSource.class.getSimpleName());
        @Override
        public Observable<ProjectSourceFetchResult> fetchProjects() {
            LOG.warn("Using empty project source to fetch projects");
            return Observable.empty();
        }

        @Override
        public Observable<ProjectApkSourceFetchResult> fetchApkSources(ProjectOverview projectOverview) {
            LOG.warn("Using empty project source to fetch apk sources for {}", projectOverview);
            return Observable.empty();
        }

        @Override
        public Observable<ProjectApkSource> fetchApkSourceDetails(ProjectApkSource apkSourceAt) {
            LOG.warn("Using empty project source to fetch apk source details {}", apkSourceAt);
            return Observable.empty();
        }

        @Override
        public boolean hasProject(ProjectOverview projectOverview) {
            LOG.warn("Using empty project source to check for hasProject {}", projectOverview);
            return false;
        }
    }
}
