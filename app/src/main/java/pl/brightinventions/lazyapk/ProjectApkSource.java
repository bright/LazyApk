package pl.brightinventions.lazyapk;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import pl.brightinventions.lazyapk.teamcity.MasterDetailsViewHolderOf;
import rx.Observable;

public interface ProjectApkSource extends Serializable {
    String getId();
    MasterDetailsViewHolderOf<ProjectApkSource> createListItemViewHolder(LayoutInflater layoutInflater, ViewGroup parent, ProjectSource projectSource);
    Observable<ProjectApkSource> fetchDetailsCachedOrNewWithDelay(ProjectSource projectSource, int i, TimeUnit milliseconds);
    Intent newDetailsIntent(Context activity);
}
