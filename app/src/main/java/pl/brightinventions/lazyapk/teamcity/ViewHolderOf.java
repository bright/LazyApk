package pl.brightinventions.lazyapk.teamcity;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import rx.subscriptions.CompositeSubscription;

public abstract class ViewHolderOf<T> extends RecyclerView.ViewHolder {
    public ViewHolderOf(View itemView) {
        super(itemView);
    }

    final CompositeSubscription compositeSubscription = new CompositeSubscription();
    public abstract void updateWith(T apkSourceAt);
}
