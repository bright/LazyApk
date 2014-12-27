package pl.brightinventions.lazyapk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class ApplicationStateLifecycleListener implements ApplicationState, Application.ActivityLifecycleCallbacks {


    private final Handler handler;
    private WeakReference<Activity> lastActivity;
    private ActivityPausedRunnable activityPausedRunnable;

    @Inject
    ApplicationStateLifecycleListener() {
        handler = new Handler();
        activityPausedRunnable = new ActivityPausedRunnable();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        handler.removeCallbacks(activityPausedRunnable);
        lastActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        handler.removeCallbacks(activityPausedRunnable);
        handler.postDelayed(activityPausedRunnable,100);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public boolean isInForeground() {
        return lastActivity != null && lastActivity.get() != null;
    }

    private class ActivityPausedRunnable implements Runnable {
        @Override
        public void run() {
            lastActivity = null;
        }
    }
}
