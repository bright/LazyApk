package pl.brightinventions.lazyapk;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import pl.brightinventions.lazyapk.teamcity.ObservableCollection;
import pl.brightinventions.lazyapk.teamcity.ViewHolderOf;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public abstract class RecyclerViewAdapterObserver<TItem, TViewHolder extends ViewHolderOf<TItem>> extends RecyclerView.Adapter<TViewHolder> {
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private ArrayList<TItem> items;

    protected RecyclerViewAdapterObserver(ObservableCollection<TItem> collection) {
        this(collection, 0, TimeUnit.MICROSECONDS);
    }

    protected RecyclerViewAdapterObserver(ObservableCollection<TItem> collection, int delay, TimeUnit delayUnit) {
        items = Linq.toList(collection);
        addSubscription(collection.itemInsertedAt()
                .delay(delay, delayUnit, AndroidSchedulers.mainThread()).subscribe(new Action1<Pair<TItem, Integer>>() {
                    @Override
                    public void call(Pair<TItem, Integer> tItemIntegerPair) {
                        items.add(tItemIntegerPair.second, tItemIntegerPair.first);
                        notifyItemInserted(tItemIntegerPair.second);
                    }
                }));
        addSubscription(collection.itemsAtRangeRemoved()
                .delay(delay, delayUnit, AndroidSchedulers.mainThread()).subscribe(new Action1<Pair<Integer, Integer>>() {
                    @Override
                    public void call(Pair<Integer, Integer> integerIntegerPair) {
                        int start = integerIntegerPair.first;
                        int count = integerIntegerPair.second;
                        for (int itemToRemove = start + count - 1;
                             itemToRemove >= start;
                             itemToRemove -= 1) {
                            items.remove(itemToRemove);
                        }
                        notifyItemRangeRemoved(start, count);
                    }
                }));
    }

    public void addSubscription(Subscription subscription) {
        compositeSubscription.add(subscription);
    }

    public TItem getItem(int position) {
        return items.get(position);
    }

    public Subscription getSubscription() {
        return compositeSubscription;
    }

    public void removeSubscription(Subscription subscription) {
        compositeSubscription.remove(subscription);
    }

    public final int getItemCount() {
        return items.size();
    }

    protected void setEventsDelay(int i, TimeUnit milliseconds) {

    }
}
