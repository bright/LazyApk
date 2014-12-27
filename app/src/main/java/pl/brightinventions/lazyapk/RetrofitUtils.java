package pl.brightinventions.lazyapk;

import android.os.Build;

import retrofit.android.AndroidApacheClient;
import retrofit.client.Client;
import retrofit.client.UrlConnectionClient;

public class RetrofitUtils {
    public static Client defaultClient() {
        final Client client;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            client = new AndroidApacheClient();
        } else {
            client = new UrlConnectionClient();
        }
        return client;
    }
}
