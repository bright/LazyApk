package pl.brightinventions.lazyapk;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;

import rx.Observable;
import rx.Subscriber;
import rx.android.subscriptions.AndroidSubscriptions;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class DownloadContentObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    private PublishSubject<Uri> contentChanged = PublishSubject.create();
    public DownloadContentObserver() {
        super(null);
    }


    @Override
    public void onChange(boolean selfChange, Uri uri) {
        contentChanged.onNext(uri);
    }
    private final static Uri MY_DOWNLOADS = Uri.parse( "content://downloads/my_downloads" );

    public static Observable<Uri> observeDownloadContentChanged(final Context context, final long downloadId){
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(final Subscriber<? super Uri> subscriber) {
                final DownloadContentObserver observer = new DownloadContentObserver();
                final ContentResolver resolver = context.getContentResolver();
                resolver.registerContentObserver(MY_DOWNLOADS, true, observer);

                subscriber.add(AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                    @Override
                    public void call() {
                        resolver.unregisterContentObserver(observer);
                    }
                }));

                subscriber.add(observer.contentChanged.subscribe(new Action1<Uri>() {
                    @Override
                    public void call(Uri uri) {
                        if(uri.getPath().endsWith(String.valueOf(downloadId))) {
                            subscriber.onNext(uri);
                        }
                    }
                }));
            }
        });
    }
}
