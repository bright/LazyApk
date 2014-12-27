package pl.brightinventions.lazyapk;

import android.net.Uri;
import android.text.TextUtils;

public class DownloadAbleApkProgress {
    private final DownloadAbleApk downloadAbleApk;
    private double progress;
    private boolean completedWithSuccess;
    private String completedWithError;
    private boolean started;

    public DownloadAbleApkProgress(DownloadAbleApk downloadAbleApk) {
        this.downloadAbleApk = downloadAbleApk;
    }

    @Override
    public String toString() {
        return "DownloadAbleApkProgress{" +
                "apk=" + downloadAbleApk +
                ", progress=" + progress +
                ", completedWithSuccess=" + completedWithSuccess +
                '}';
    }

    public void updateProgress(long bytesSoFar, long totalBytes) {
        if (totalBytes <= 0) {
            setProgress(0);
        } else {
            setStarted();
            setProgress(bytesSoFar * 1.0 / totalBytes);
        }
    }

    private void setStarted() {
        this.started = true;
    }

    public boolean isStarted(){
        return started;
    }

    private void setProgress(double progress) {
        this.progress = progress;
    }

    public void setCompletedOk() {
        this.completedWithSuccess = true;
        this.completedWithError = null;
    }

    public void setCompletedWithError(String completedWithError) {
        this.completedWithError = completedWithError;
        this.completedWithSuccess = false;
    }

    public boolean isCompletedWithSuccess() {
        return completedWithSuccess;
    }
    public boolean isCompletedWithError() {
        return !TextUtils.isEmpty(completedWithError);
    }


    public boolean isCompleted(){
        return isCompletedWithSuccess() || isCompletedWithError();
    }

    public Uri getDownloadedApkFileUri() {
        return downloadAbleApk.getDownloadedFileUri();
    }

    public String getDownloadedApkId() {
        return downloadAbleApk.getId();
    }
}
