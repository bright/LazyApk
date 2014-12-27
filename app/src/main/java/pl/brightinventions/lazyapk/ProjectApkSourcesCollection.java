package pl.brightinventions.lazyapk;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import pl.brightinventions.lazyapk.teamcity.MasterDetailsViewHolderOf;

public class ProjectApkSourcesCollection {
    private final ProjectSource projectSource;
    private ArrayList<ProjectApkSource> apkSources = new ArrayList<>();

    public ProjectApkSourcesCollection(ProjectSource projectSource) {
        this.projectSource = projectSource;
    }

    public int getCount() {
        return apkSources.size();
    }

    public void addAll(Iterable<ProjectApkSource> results){
        apkSources = Linq.toList(results);
    }
    public void setApkSources(Iterable<ProjectApkSource> results){
        apkSources = Linq.toList(results);
    }

    public MasterDetailsViewHolderOf<ProjectApkSource> createListItemViewHolder(LayoutInflater layoutInflater, ViewGroup parent) {
        return apkSources.get(0).createListItemViewHolder(layoutInflater, parent, projectSource);
    }

    public void bindViewHolder(MasterDetailsViewHolderOf<ProjectApkSource> holder, int position) {
        ProjectApkSource apkSourceAt = apkSources.get(position);
        holder.updateWith(apkSourceAt);
    }
}
