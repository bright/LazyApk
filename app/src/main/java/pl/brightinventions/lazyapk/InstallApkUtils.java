package pl.brightinventions.lazyapk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallApkUtils {
    private static final Logger LOG = LoggerFactory.getLogger(InstallApkUtils.class.getSimpleName());
    public static void startInstall(Uri apkFileUri, Context context) {
        Intent playIntent = new Intent(Intent.ACTION_VIEW);
        playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(apkFileUri.toString()));
        playIntent.setDataAndType(apkFileUri, mimeTypeFromExtension);
        try {
            LOG.info("Start install of {}", apkFileUri);
            context.startActivity(playIntent);
        } catch (Exception e) {
            LOG.error("Failed to start apk install {}", apkFileUri, e);
        }

    }
}
