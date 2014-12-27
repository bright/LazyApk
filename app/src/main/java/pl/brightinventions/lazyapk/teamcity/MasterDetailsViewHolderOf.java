package pl.brightinventions.lazyapk.teamcity;

import android.view.View;

import rx.Subscription;

public abstract class MasterDetailsViewHolderOf<T> extends ViewHolderOf<T> {
    public MasterDetailsViewHolderOf(View itemView) {
        super(itemView);
    }
    private Subscription loaderSubscription;

    public Subscription getLoaderSubscription() {
        return loaderSubscription;
    }

    public void setDetailsLoadSubscription(Subscription subscription){
        if(loaderSubscription != null){
            loaderSubscription.unsubscribe();
        }
        loaderSubscription = subscription;
    }
}
