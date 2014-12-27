package pl.brightinventions.lazyapk;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DownloadHistory {
    private final SharedPreferences apkIdToDownloadId;

    @Inject
    public DownloadHistory(@AppContext Context context) {
        this.apkIdToDownloadId = context.getSharedPreferences("ApkIdToDownloadId", Context.MODE_PRIVATE);
    }

    public boolean wasDownloadedInPast(String id) {
        return apkIdToDownloadId.contains(id);
    }

    public void downloadStarted(String id, long downloadId) {
        apkIdToDownloadId.edit()
                .putLong(id, downloadId)
                .putString(stateKey(id), "IN_PROGRESS")
                .apply();
    }

    public void downloadCompletedSuccess(String id, long downloadId) {
        apkIdToDownloadId.edit()
                .putLong(id, downloadId)
                .putString(stateKey(id), "SUCCESS")
                .apply();
    }

    public void downloadCompletedWithError(String id) {
        apkIdToDownloadId.edit()
                .remove(id)
                .putString(stateKey(id), "FAILURE")
                .apply();
    }

    public long getDownloadId(String id) {
        return apkIdToDownloadId.getLong(id, 0);
    }

    private String stateKey(String id) {
        return id + "_state";
    }

    public boolean isSuccess(String id) {
        return apkIdToDownloadId.getString(stateKey(id), "FAILURE").equalsIgnoreCase("SUCCESS");
    }
}
