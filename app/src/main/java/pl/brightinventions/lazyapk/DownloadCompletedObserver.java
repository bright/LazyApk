package pl.brightinventions.lazyapk;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscriber;

public abstract class DownloadCompletedObserver extends BroadcastReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadCompletedObserver.class.getSimpleName());
    public static Observable<Long> observeDownloadCompleted(final Context context, final long downloadId) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(final Subscriber<? super Long> subscriber) {
                DownloadCompletedObserver observer = new DownloadCompletedObserver(){
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        long intentDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                        if(intentDownloadId == downloadId){
                            LOG.trace("Download completed {} (action {})", intentDownloadId, intent.getAction());
                            context.unregisterReceiver(this);
                            subscriber.onNext(downloadId);
                            subscriber.onCompleted();
                        }
                    }
                };
                observer.register(context);
            }
        });
    }

    private void register(Context context) {
        context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

}
