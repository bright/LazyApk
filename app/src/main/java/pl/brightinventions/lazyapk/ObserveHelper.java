package pl.brightinventions.lazyapk;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

public class ObserveHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ObserveHelper.class.getSimpleName());

    public static Subscription bindVisibility(Observable<Boolean> booleanObservable, final View view) {
        return booleanObservable.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

    public static Subscription bindText(Observable<String> observable, final TextView text) {
        return observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                LOG.trace("Set text of {} to {}", text, s);
                text.setText(s);
            }
        });
    }

    public static Subscription showProgressUntilComplete(final ProgressBar loader, Observable<Void> voidObservable) {
        loader.setVisibility(View.VISIBLE);
        return voidObservable.subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                loader.setVisibility(View.GONE);
            }
        }, new Action0() {
            @Override
            public void call() {
                loader.setVisibility(View.GONE);
            }
        });
    }
}
