package pl.brightinventions.lazyapk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.functions.Func1;

public class Linq {
    public static  <TResult,TSource>  Iterable<TResult> map(Iterable<TSource> sourceIterable, Func1<TSource,TResult> mapper){
        ArrayList<TResult> result = new ArrayList<>();
        for(TSource source: sourceIterable){
            result.add(mapper.call(source));
        }
        return result;
    }

    public static <TItem> boolean any(Iterable<TItem> items, Func1<TItem, Boolean> predicate) {
        for(TItem item:items){
            if(predicate.call(item)){
                return true;
            }
        }
        return false;
    }

    public static <TItem> Iterable<TItem> filter(Iterable<TItem> items, Func1<TItem, Boolean> predicate) {
        ArrayList<TItem> result = new ArrayList<>();
        for(TItem item:items){
            if(predicate.call(item)){
                result.add(item);
            }
        }
        return result;
    }

    public static <TItem> Collection<TItem> addAll(Collection<TItem> targetCollection, Iterable<? extends TItem> itemsToAdd) {
        for (TItem item:itemsToAdd){
            targetCollection.add(item);
        }
        return targetCollection;
    }

    public static <TItem> ArrayList<TItem> toList(Iterable<TItem> items) {
        ArrayList<TItem> result = new ArrayList<>();
        for(TItem item:items){
            result.add(item);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <TItem> TItem[] toArray(List<TItem> items) {
        TItem[] array = (TItem[]) new Object[items.size()];
        return items.toArray(array);
    }

    public static <TItem> TItem findFirst(Iterable<TItem> items, Func1<TItem, Boolean> predicate, TItem defaultValue) {
        for(TItem item:items){
            if(predicate.call(item)){
                return item;
            }
        }
        return defaultValue;
    }

    public static <T> Iterable<T> from(T result) {
        ArrayList<T> items = new ArrayList<>(1);
        items.add(result);
        return items;
    }
}
