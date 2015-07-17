package pl.brightinventions.lazyapk;

import android.app.Application;
import android.util.Pair;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import javax.inject.Inject;

import pl.brightinventions.lazyapk.sources.ProjectSourceConfiguration;
import pl.brightinventions.lazyapk.sources.ProjectSourceConfigurationCollection;
import pl.brightinventions.lazyapk.sources.ProjectSourceFactory;
import pl.brightinventions.slf4android.LoggerConfiguration;
import pl.brightinventions.slf4android.NotifyDeveloperHandler;
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
        LoggerConfiguration configuration = LoggerConfiguration.configuration();
        NotifyDeveloperHandler notifyDeveloperHandler = configuration.notifyDeveloperHandler(this, "team@brightinventions.pl");
        notifyDeveloperHandler.setFilter(new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                return false;
            }
        });
        notifyDeveloperHandler.notifyWhenDeviceIsShaken();
        configuration.addHandlerToRootLogger(notifyDeveloperHandler);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                .build());

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
