package pl.brightinventions.lazyapk.setup;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;

import pl.brightinventions.lazyapk.R;
import pl.brightinventions.lazyapk.RefreshBehavior;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class RefreshableSetup {
    public static void setupAndInitRefresh(CompositeSubscription subscription, final RefreshBehavior viewModel, final SwipeRefreshLayout refreshable) {
        subscription.add(viewModel.observeIsRefreshingChanged().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                refreshable.setRefreshing(aBoolean);
            }
        }));
        forceShowRefreshIndicator(refreshable);
        refreshable.setColorSchemeResources(R.color.primary, R.color.primary_dark);
        refreshable.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.refresh();
            }
        });
        viewModel.refresh();
    }

    public static void forceShowRefreshIndicator(SwipeRefreshLayout refreshable) {
        TypedValue typed_value = new TypedValue();
        Context context = refreshable.getContext();
        context.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        refreshable.setProgressViewOffset(false, 0, context.getResources().getDimensionPixelSize(typed_value.resourceId));
    }
}
