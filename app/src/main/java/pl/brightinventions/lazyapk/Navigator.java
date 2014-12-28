package pl.brightinventions.lazyapk;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import pl.brightinventions.lazyapk.sources.SourcesActivity;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class Navigator {
    private static final Logger LOG = LoggerFactory.getLogger(Navigator.class.getSimpleName());

    @Inject
    public Navigator() {
    }

    public void showBuildSources(Activity currentActivity) {
        currentActivity.startActivity(new Intent(currentActivity, SourcesActivity.class));
        enterTransition(currentActivity);
    }

    private void enterTransition(Activity currentActivity) {
        currentActivity.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    public Subscription observeOpenProjectDetails(Observable<ProjectOverview> projectDetails, final Activity activity) {
        return projectDetails.subscribe(new Action1<ProjectOverview>() {
            @Override
            public void call(ProjectOverview projectOverview) {
                Intent intent = ProjectDetailsActivity.newIntent(projectOverview, activity);
                startActivity(intent, activity);
            }
        });
    }

    private void startActivity(Intent intent, Activity activity) {
        startActivity(intent, activity, null);
    }

    private void startActivity(Intent intent, Activity activity, View sharedView) {
        LOG.trace("Will start intent {} from activity {} (shared view: {})", intent, activity, sharedView);
        if (Build.VERSION.SDK_INT >= 16) {
            activity.startActivity(intent, makeSceneTransition(activity, sharedView));
        } else {
            activity.startActivity(intent);
            enterTransition(activity);
        }
    }

    private Bundle makeSceneTransition(Activity activity, View sharedView) {
        if (Build.VERSION.SDK_INT >= 21) {
            //noinspection unchecked
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
            if (sharedView != null) {
                options = ActivityOptions.makeSceneTransitionAnimation(activity, sharedView, sharedView.getTransitionName());
            }
            return options.toBundle();
        } else {
            return null;
        }

    }

    public Subscription observeOpenApkSource(Observable<ProjectApkSource> observable, final Activity activity, final Func1<ProjectApkSource, View> sharedViewFactory) {
        return observable.subscribe(new Action1<ProjectApkSource>() {
            @Override
            public void call(ProjectApkSource projectApkSource) {
                Intent intent = projectApkSource.newDetailsIntent(activity);
                startActivity(intent, activity, sharedViewFactory.call(projectApkSource));
            }
        });
    }

    public void openSources(Activity activity) {
        startActivity(SourcesActivity.newIntent(activity), activity);
    }
}
