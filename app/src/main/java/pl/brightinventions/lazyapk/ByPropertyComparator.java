package pl.brightinventions.lazyapk;

import java.util.Comparator;

import rx.functions.Func1;

public class ByPropertyComparator<T, TProperty extends Comparable<TProperty>> implements Comparator<T> {
    private final Func1<T,TProperty> toCompare;

    public ByPropertyComparator(Func1<T, TProperty> toCompare) {
        this.toCompare = toCompare;
    }

    public static <TItem,TOther extends Comparable<TOther>> Comparator<TItem> of(Func1<TItem, TOther> func1) {
        return new ByPropertyComparator<>(func1);
    }

    @Override
    public int compare(T lhs, T rhs) {
        TProperty left = toCompare.call(lhs);
        TProperty right = toCompare.call(rhs);
        return left.compareTo(right);
    }
}
