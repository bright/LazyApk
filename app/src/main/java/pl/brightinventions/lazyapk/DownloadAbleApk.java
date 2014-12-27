package pl.brightinventions.lazyapk;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DownloadAbleApk implements Serializable {
    private final String url;
    private String name;
    private String uniqueFileName;
    private ArrayList<Pair<String, String>> requestHeaders = new ArrayList<>();
    private String downloadedFileUri;

    public DownloadAbleApk(String url, String uniqueFileName) {
        this.url = url;
        this.uniqueFileName = uniqueFileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameOrFileName() {
        if(TextUtils.isEmpty(name)){
            return uniqueFileName;
        }
        return name;
    }

    public void setUniqueFileName(String uniqueFileName) {
        this.uniqueFileName = uniqueFileName;
    }

    public String getUniqueFileName() {
        return uniqueFileName;
    }

    public Uri getUri() {
        return Uri.parse(url);
    }

    public String getId(){
        return getUniqueFileName();
    }

    public List<Pair<String, String>> getRequestHeaders() {
        return requestHeaders;
    }

    public void addRequestHeader(String headerName, String value) {
        requestHeaders.add(Pair.create(headerName, value));
    }

    @Override
    public String toString() {
        return "DownloadAbleApk{" +
                "uniqueFileName='" + uniqueFileName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public void setDownloadedFileUri(Uri downloadedFileUri) {
        this.downloadedFileUri = downloadedFileUri.toString();
    }

    public Uri getDownloadedFileUri() {
        return Uri.parse(downloadedFileUri);
    }
}
