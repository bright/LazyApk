package pl.brightinventions.lazyapk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import javax.inject.Inject;

import butterknife.InjectView;
import pl.brightinventions.lazyapk.setup.ActivitySetup;
import pl.brightinventions.lazyapk.setup.RecyclerViewSetup;
import pl.brightinventions.lazyapk.setup.RefreshableSetup;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProjectDetailsActivity extends ActionBarActivity {
    @InjectView(R.id.toolbar) Toolbar toolbar;

    SerializableSupplier<ProjectOverview> projectOverview = new RequiredIntentValueSupplier<>(this, "overview");

    @Inject Navigator navigator;

    @Inject ProjectDetailsViewModel projectDetailsViewModel;

    CompositeSubscription subscription;

    @InjectView(R.id.refreshable) SwipeRefreshLayout refreshable;
    @InjectView(R.id.apkSourceList) RecyclerView apkSourceList;


    public static Intent newIntent(ProjectOverview projectOverview, Context context) {
        Intent intent = new Intent(context, ProjectDetailsActivity.class);
        intent.putExtra("overview", projectOverview);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetup.setup(this, R.layout.project_details);
        ProjectOverview overview = projectOverview.get();

        ToolbarSetup.asSupportWithUp(toolbar, overview.getName(), this);

        subscription = new CompositeSubscription();

        projectDetailsViewModel.setProjectOverview(overview);

        subscription.add(navigator.observeOpenApkSource(
                projectDetailsViewModel.openApkSource(),
                this,
                projectDetailsViewModel.sharedViewFactory()
        ));

        RecyclerViewSetup.linearWithDivider(apkSourceList, projectDetailsViewModel.createApkSourceListAdapter());

        RefreshableSetup.setupAndInitRefresh(subscription, projectDetailsViewModel.getRefreshBehavior(), refreshable);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }
}
