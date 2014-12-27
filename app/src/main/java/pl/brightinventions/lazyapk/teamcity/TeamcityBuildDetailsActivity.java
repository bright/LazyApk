package pl.brightinventions.lazyapk.teamcity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import javax.inject.Inject;

import butterknife.InjectView;
import pl.brightinventions.lazyapk.R;
import pl.brightinventions.lazyapk.RecyclerViewAdapterObserver;
import pl.brightinventions.lazyapk.RequiredIntentValueSupplier;
import pl.brightinventions.lazyapk.ToolbarSetup;
import pl.brightinventions.lazyapk.setup.ActivitySetup;
import pl.brightinventions.lazyapk.setup.RecyclerViewSetup;
import pl.brightinventions.lazyapk.setup.RefreshableSetup;
import rx.subscriptions.CompositeSubscription;

public class TeamcityBuildDetailsActivity extends ActionBarActivity {

    CompositeSubscription subscription;

    @InjectView(R.id.refreshable) SwipeRefreshLayout refreshable;
    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.apkList) RecyclerView apkList;

    @InjectView(android.R.id.content) View content;
    @Inject TeamcityBuildDetailsViewModel viewModel;

    public static Intent newIntent(TeamcityBuildResponse apkSource, Context context) {
        Intent intent = new Intent(context, TeamcityBuildDetailsActivity.class);
        intent.putExtra("buildResponseSupplier", apkSource);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscription = new CompositeSubscription();
        ActivitySetup.setup(this, R.layout.teamcity_build_details);

        RequiredIntentValueSupplier<TeamcityBuildResponse> buildResponseSupplier = new RequiredIntentValueSupplier<>(this, "buildResponseSupplier");

        viewModel.initWith(buildResponseSupplier.get());

        subscription.add(viewModel.bindBuildInfo(content));

        ToolbarSetup.asSupportWithUp(toolbar, viewModel.getTitle(), this);

        RecyclerViewAdapterObserver adapter = viewModel.createApkListAdapter();

        subscription.add(adapter.getSubscription());

        RecyclerViewSetup.linearWithDivider(apkList, adapter);

        RefreshableSetup.setupAndInitRefresh(subscription, viewModel.getRefreshBehavior(), refreshable);

    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }
}
