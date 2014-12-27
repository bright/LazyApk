package pl.brightinventions.lazyapk;


import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class RefreshBehavior {
    private final Toaster toaster;
    private BehaviorSubject<Boolean> observeIsRefreshing = BehaviorSubject.create(false);
    private Func0<Observable<?>> refreshAction;

    @Inject
    public RefreshBehavior(Toaster toaster) {
        this.toaster = toaster;
    }

    public Observable<Boolean> observeIsRefreshingChanged(){
        return observeIsRefreshing;
    }

    public Subscription refresh() {
        observeIsRefreshing.onNext(true);
        return refreshAction.call().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                toaster.onError(throwable);
                observeIsRefreshing.onNext(false);
            }
        }, new Action0() {
            @Override
            public void call() {
                observeIsRefreshing.onNext(false);
            }
        });
    }

    public void whenRefreshingDo(Func0<Observable<?>> makeObservable) {
        refreshAction = makeObservable;
    }
}
