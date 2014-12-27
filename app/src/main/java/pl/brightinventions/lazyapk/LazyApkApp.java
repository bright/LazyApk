package pl.brightinventions.lazyapk;

import android.app.Application;
import android.util.Pair;

import javax.inject.Inject;

import pl.brightinventions.lazyapk.sources.ProjectSourceConfiguration;
import pl.brightinventions.lazyapk.sources.ProjectSourceConfigurationCollection;
import pl.brightinventions.lazyapk.sources.ProjectSourceFactory;
import rx.functions.Action1;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class LazyApkApp extends Application {
    @Inject
    ApplicationStateLifecycleListener lifecycleListener;

    @Inject
    ProjectSourceFactory projectSourceFactory;

    @Inject
    ProjectSources projectSources;

    @Inject
    ProjectSourceConfigurationCollection configurations;

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(R.attr.fontPath);

        DependencyGraph.addModule(new LazyApkModule(this));

        DependencyGraph.inject(this);

        registerActivityLifecycleCallbacks(lifecycleListener);

        initProjectSources();

        configurations.itemInsertedAt().subscribe(new Action1<Pair<ProjectSourceConfiguration, Integer>>() {
            @Override
            public void call(Pair<ProjectSourceConfiguration, Integer> projectSourceConfigurationIntegerPair) {
                initProjectSources();
            }
        });
        configurations.itemsAtRangeRemoved().subscribe(new Action1<Pair<Integer, Integer>>() {
            @Override
            public void call(Pair<Integer, Integer> integerIntegerPair) {
                initProjectSources();
            }
        });
    }

    private void initProjectSources() {
        projectSources.removeAll();
        for(ProjectSourceConfiguration configuration: configurations){
            projectSources.addProjectSource(projectSourceFactory.create(configuration));
        }
    }

}
