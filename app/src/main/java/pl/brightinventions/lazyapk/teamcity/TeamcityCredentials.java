package pl.brightinventions.lazyapk.teamcity;

import android.util.Pair;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;

import java.util.ArrayList;

public class TeamcityCredentials {
    private final String userName;
    private final String password;

    public TeamcityCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public void addCredentialsTo(Request.Builder builder) {
        builder.header("Authorization", Credentials.basic(userName, password));
    }

    public Iterable<Pair<String, String>> getHeaders() {
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(Pair.create("Authorization", Credentials.basic(userName, password)));
        return headers;
    }
}
