package pl.brightinventions.lazyapk;

import java.util.Objects;

import rx.functions.Func1;

public class Predicates {
    public static <TItem> Func1<TItem,Boolean> equalTo(final TItem mustEqualTo) {
        return new Func1<TItem, Boolean>() {
            @Override
            public Boolean call(TItem item) {
                return Objects.equals(item, mustEqualTo);
            }
        };
    }
}
