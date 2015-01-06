package pl.brightinventions.lazyapk;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import pl.brightinventions.lazyapk.setup.ActivitySetup;
import pl.brightinventions.lazyapk.setup.RecyclerViewSetup;
import pl.brightinventions.lazyapk.setup.RefreshableSetup;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class HomeActivity extends ActionBarActivity {
    private static final Logger LOG = LoggerFactory.getLogger(HomeActivity.class.getSimpleName());
    @InjectView(R.id.toolbar) Toolbar toolbar;

    @InjectView(R.id.mainList) RecyclerView mainList;
    @InjectView(R.id.refreshable) SwipeRefreshLayout refreshable;
    @InjectView(R.id.emptyBuildsContainer) View emptyBuildsContainer;

    @Inject Navigator navigator;

    @Inject HomeActivityViewModel homeActivityViewModel;

    CompositeSubscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetup.setup(this, R.layout.home);
        setSupportActionBar(toolbar);

        RecyclerViewSetup.linearWithDivider(mainList, homeActivityViewModel.createProjectListAdapter(this));

        subscription = new CompositeSubscription();

        subscription.add(homeActivityViewModel.observeEmptyProjects().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                emptyBuildsContainer.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
            }
        }));

        subscription.add(navigator.observeOpenProjectDetails(homeActivityViewModel.openProjectDetails(),this));

        RefreshableSetup.setupAndInitRefresh(subscription, homeActivityViewModel.getRefreshBehavior(), refreshable);
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        subscription = null;
        super.onDestroy();
    }

    @OnClick(R.id.sources)
    public void onGoToSources() {
        navigator.openSources(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeActivityViewModel.refreshIfEmpty();
    }

    @OnClick(R.id.addBuildSource)
    public void onAddBuildSource() {
        navigator.showBuildSources(this);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        LOG.trace("attachBaseContext {}", newBase);
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }
}
