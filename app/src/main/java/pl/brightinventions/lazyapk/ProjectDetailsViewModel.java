package pl.brightinventions.lazyapk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import pl.brightinventions.lazyapk.teamcity.MasterDetailsViewHolderOf;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ProjectDetailsViewModel implements RefreshSource {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectDetailsViewModel.class.getSimpleName());
    private final ProjectSources projectSources;
    private ApkSourceListAdapter adapter;
    private ProjectOverview projectOverview;
    private ProjectSource projectSource;
    private ProjectApkSourcesCollection projectApkSourcesCollection;
    private RefreshBehavior refreshBehavior;
    private PublishSubject<ProjectApkSource> openApkSource = PublishSubject.create();
    private MasterDetailsViewHolderOf<ProjectApkSource> lastClickedViewHolder;

    @Inject
    public ProjectDetailsViewModel(ProjectSources projectSources, RefreshBehavior refreshBehavior) {
        this.projectSources = projectSources;
        this.refreshBehavior = refreshBehavior;
        this.refreshBehavior.whenRefreshingDo(new Func0<Observable<?>>() {
            @Override
            public Observable<?> call() {
                return refresh();
            }
        });
    }

    public Observable<ProjectApkSource> openApkSource(){
        return openApkSource;
    }

    private Observable<ProjectApkSourceFetchResult> refresh() {
        return this.projectSource.fetchApkSources(projectOverview).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<ProjectApkSourceFetchResult>() {
                    @Override
                    public void call(ProjectApkSourceFetchResult value) {
                        int count = projectApkSourcesCollection.getCount();
                        LOG.trace("projectApkSourcesCollection -> notifyItemRangeRemoved");
                        adapter.notifyItemRangeRemoved(0, count);
                        projectApkSourcesCollection.setApkSources(value.getResults());
                        LOG.trace("projectApkSourcesCollection -> notifyItemRangeInserted");
                        adapter.notifyItemRangeInserted(0, projectApkSourcesCollection.getCount());
                    }
                });
    }

    public RecyclerView.Adapter createApkSourceListAdapter() {
        adapter = new ApkSourceListAdapter();
        return adapter;
    }

    @Override
    public RefreshBehavior getRefreshBehavior() {
        return refreshBehavior;
    }

    public void setProjectOverview(ProjectOverview projectOverview) {
        this.projectOverview = projectOverview;
        this.projectSource = projectSources.getSourceFor(projectOverview);
        this.projectApkSourcesCollection = new ProjectApkSourcesCollection(this.projectSource);
    }

    public Func1<ProjectApkSource, View> sharedViewFactory() {
        return new Func1<ProjectApkSource, View>() {
            @Override
            public View call(ProjectApkSource projectApkSource) {
                if(lastClickedViewHolder != null){
                    View sharedView = lastClickedViewHolder.itemView;
                    lastClickedViewHolder = null;
                    return sharedView;
                }
                return null;
            }
        };
    }

    private class ApkSourceListAdapter extends RecyclerView.Adapter<MasterDetailsViewHolderOf<ProjectApkSource>> {
        private static final int PROJECT_APK_SOURCE = 1;

        @Override
        public MasterDetailsViewHolderOf<ProjectApkSource> onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            final MasterDetailsViewHolderOf<ProjectApkSource> itemViewHolder = projectApkSourcesCollection.createListItemViewHolder(layoutInflater, parent);
            ClickableItemOf.Helper.ifClickable(itemViewHolder, new Action1<ClickableItemOf<ProjectApkSource>>() {
                @Override
                public void call(ClickableItemOf<ProjectApkSource> clickableItemOf) {
                    clickableItemOf.onClick()
                            .doOnNext(new Action1<ProjectApkSource>() {
                                @Override
                                public void call(ProjectApkSource projectApkSource) {
                                    lastClickedViewHolder = itemViewHolder;
                                }
                            })
                            .subscribe(openApkSource);
                }
            });
            return itemViewHolder;
        }

        @Override
        public void onBindViewHolder(MasterDetailsViewHolderOf<ProjectApkSource> holder, int position) {
            projectApkSourcesCollection.bindViewHolder(holder, position);
        }

        @Override
        public int getItemViewType(int position) {
            return PROJECT_APK_SOURCE;
        }

        @Override
        public int getItemCount() {
            return projectApkSourcesCollection.getCount();
        }
    }
}
