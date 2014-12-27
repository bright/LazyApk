package pl.brightinventions.lazyapk;

import rx.functions.Func1;

public class Funcs {
    public static <TSource, TResult> Func1<TSource,TResult> always(final TResult result) {
        return new Func1<TSource, TResult>() {
            @Override
            public TResult call(TSource tSource) {
                return result;
            }
        };
    }

    public static <TSource extends TDest, TDest> Func1<TSource,TDest> changeType() {
        return new Func1<TSource, TDest>() {
            @Override
            public TDest call(TSource tSource) {
                return tSource;
            }
        };
    }
}
