package pl.brightinventions.lazyapk;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import pl.brightinventions.lazyapk.sources.SourcesActivity;
import pl.brightinventions.lazyapk.teamcity.TeamcityBuildDetailsActivity;

@Module(injects = {
        LazyApkApp.class,
        HomeActivity.class,
        SourcesActivity.class,
        ProjectDetailsActivity.class,
        TeamcityBuildDetailsActivity.class,
        SourcesActivity.class
})
public class LazyApkModule {
    public LazyApkModule(Context appContext) {
        this.appContext = appContext;
    }

    private Context appContext;

    @AppContext
    @Provides
    public Context providesAppContext(){
        return appContext;
    }

    @Provides
    public ApplicationState provideApplicationState(ApplicationStateLifecycleListener state){
         return state;
    }
}
