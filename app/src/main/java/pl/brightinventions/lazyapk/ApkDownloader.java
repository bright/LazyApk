package pl.brightinventions.lazyapk;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class ApkDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(ApkDownloader.class.getSimpleName());
    private final Context context;
    private final DownloadHistory downloadHistory;
    private final DownloadManager downloadManager;

    @Inject
    public ApkDownloader(@AppContext Context context, DownloadHistory downloadHistory) {
        this.context = context;
        this.downloadHistory = downloadHistory;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public Observable<DownloadAbleApkProgress> downloadStart(final DownloadAbleApk downloadAbleApk) {
        return whenDownloadDirIsOk(new Func1<File, Observable<DownloadAbleApkProgress>>() {
            @Override
            public Observable<DownloadAbleApkProgress> call(final File downloadDir) {
                File targetFile = new File(downloadDir, downloadAbleApk.getUniqueFileName());
                downloadAbleApk.setDownloadedFileUri(Uri.fromFile(targetFile));
                if (downloadHistory.wasDownloadedInPast(downloadAbleApk.getId())) {
                    if (targetFile.exists()) {
                        long downloadId = downloadHistory.getDownloadId(downloadAbleApk.getId());
                        LOG.info("Found existing target file at {} which seems like a previous download {}", targetFile, downloadId);
                        return getAndObserveDownloadState(downloadId, downloadAbleApk);
                    }
                }

                if (targetFile.exists()) {
                    LOG.info("Target file {} exists removed it: {}", targetFile, targetFile.delete());
                }

                DownloadManager.Request request = buildDownloadRequest(targetFile, downloadAbleApk);

                final long downloadId = startDownload(request, downloadAbleApk);

                return getAndObserveDownloadState(downloadId, downloadAbleApk);
            }
        });
    }

    private Observable<DownloadAbleApkProgress> whenDownloadDirIsOk(Func1<File, Observable<DownloadAbleApkProgress>> fileAction1) {
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (filesDir == null) {
            return Observable.error(new FailedToGetExternalDownloadDirectory());
        }
        if (!filesDir.exists() && !filesDir.mkdirs()) {
            return Observable.error(new FailedToGetExternalDownloadDirectory());
        }
        return fileAction1.call(filesDir);
    }

    private Observable<DownloadAbleApkProgress> getAndObserveDownloadState(final long downloadId, final DownloadAbleApk downloadAbleApk) {
        return Observable.create(new GetAndObserveDownloadState(downloadId, downloadAbleApk))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private DownloadManager.Request buildDownloadRequest(File targetFile, DownloadAbleApk downloadAbleApk) {
        DownloadManager.Request request = new DownloadManager.Request(downloadAbleApk.getUri());
        List<Pair<String, String>> headers = downloadAbleApk.getRequestHeaders();
        if (headers != null) {
            for (Pair<String, String> header : headers) {
                request.addRequestHeader(header.first, header.second);
            }
        }
        request.setDescription(downloadAbleApk.getNameOrFileName());
        request.setTitle(downloadAbleApk.getNameOrFileName());
        request.setAllowedOverRoaming(false);
        Uri uri = Uri.fromFile(targetFile);
        request.setDestinationUri(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())));
        return request;
    }

    private long startDownload(DownloadManager.Request request, DownloadAbleApk downloadAbleApk) {
        final long downloadId = downloadManager.enqueue(request);
        downloadHistory.downloadStarted(downloadAbleApk.getId(), downloadId);
        LOG.info("Enqueued download item {} with id {}", downloadAbleApk, downloadId);
        return downloadId;
    }

    public Observable<DownloadAbleApkProgress> getAndObserveDownloadState(final DownloadAbleApk downloadAbleApk) {
        String id = downloadAbleApk.getId();
        if (downloadHistory.wasDownloadedInPast(id)) {
            long downloadId = downloadHistory.getDownloadId(id);
            return getAndObserveDownloadState(downloadId, downloadAbleApk);
        } else {
            return Observable.just(new DownloadAbleApkProgress(downloadAbleApk));
        }
    }

    private DownloadAbleApkProgress queryDownloadProgress(DownloadAbleApk downloadAbleApk, long downloadId) {
        DownloadAbleApkProgress progress = new DownloadAbleApkProgress(downloadAbleApk);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor queryResult = downloadManager.query(query);
        if (queryResult.moveToFirst()) {
            int status = queryResult.getInt(queryResult.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String reason = queryResult.getString(queryResult.getColumnIndex(DownloadManager.COLUMN_REASON));
            long bytesSoFar = queryResult.getLong(queryResult.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long totalBytes = queryResult.getLong(queryResult.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            LOG.trace("Download {} status {} progress {} reason {}", downloadId, status, bytesSoFar * 1.0 / totalBytes, reason);
            progress.updateProgress(bytesSoFar, totalBytes);
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                downloadHistory.downloadCompletedSuccess(progress.getDownloadedApkId(), downloadId);
                progress.setCompletedOk();
            } else if (status == DownloadManager.STATUS_FAILED) {
                downloadHistory.downloadCompletedWithError(progress.getDownloadedApkId());
                progress.setCompletedWithError(reason);
            }
        }
        queryResult.close();
        return progress;
    }

    private class GetAndObserveDownloadState implements Observable.OnSubscribe<DownloadAbleApkProgress> {
        private final long downloadId;
        private final DownloadAbleApk downloadAbleApk;

        public GetAndObserveDownloadState(long downloadId, DownloadAbleApk downloadAbleApk) {
            this.downloadId = downloadId;
            this.downloadAbleApk = downloadAbleApk;
        }

        @Override
        public void call(final Subscriber<? super DownloadAbleApkProgress> subscriber) {
            Observable<Uri> downloadContentChanged = DownloadContentObserver.observeDownloadContentChanged(context, downloadId);
            Observable<Long> downloadCompletedObserver = DownloadCompletedObserver.observeDownloadCompleted(context, downloadId);
            subscriber.add(downloadContentChanged.takeUntil(downloadCompletedObserver)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            DownloadAbleApkProgress progress = queryDownloadProgress(downloadAbleApk, downloadId);
                            subscriber.onNext(progress);
                        }
                    }));

            subscriber.add(downloadCompletedObserver.subscribeOn(Schedulers.io()).subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    DownloadAbleApkProgress apkProgress = queryDownloadProgress(downloadAbleApk, downloadId);
                    subscriber.onNext(apkProgress);
                    subscriber.onCompleted();
                }
            }));

            DownloadAbleApkProgress progress = queryDownloadProgress(downloadAbleApk, downloadId);
            subscriber.onNext(progress);
            if(progress.isCompleted()){
                subscriber.onCompleted();
            }
        }
    }
}
