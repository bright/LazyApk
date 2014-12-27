package pl.brightinventions.lazyapk.teamcity;

import android.util.Pair;

import java.util.Iterator;

import rx.Observable;

public interface ObservableCollection<T> extends Iterable<T> {
    int size();
    T get(int position);
    Observable<Pair<T,Integer>> itemInsertedAt();
    Observable<Pair<Integer,Integer>> itemsAtRangeRemoved();

    @Override
    Iterator<T> iterator();
}
