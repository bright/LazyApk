package pl.brightinventions.lazyapk.teamcity;

import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.brightinventions.lazyapk.ApkDownloader;
import pl.brightinventions.lazyapk.ApplicationState;
import pl.brightinventions.lazyapk.DownloadAbleApk;
import pl.brightinventions.lazyapk.DownloadAbleApkProgress;
import pl.brightinventions.lazyapk.InstallApkUtils;
import pl.brightinventions.lazyapk.ProjectSources;
import pl.brightinventions.lazyapk.R;
import pl.brightinventions.lazyapk.RecyclerViewAdapterObserver;
import pl.brightinventions.lazyapk.RefreshBehavior;
import pl.brightinventions.lazyapk.RefreshSource;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

public class TeamcityBuildDetailsViewModel implements RefreshSource {
    private static final Logger LOG = LoggerFactory.getLogger(TeamcityBuildDetailsViewModel.class.getSimpleName());
    private final RefreshBehavior refreshBehavior;
    private final ProjectSources projectSources;
    private final ApkDownloader apkDownloader;
    private final ApplicationState applicationState;
    private TeamcityProjectSource projectSource;
    private TeamcityBuildResponse buildResponse;
    private TeamcityApkArtifactAdapter adapter;
    private TeamcityBuildArtifactCollection artifactCollection;

    @Inject
    public TeamcityBuildDetailsViewModel(RefreshBehavior refreshBehavior,
                                         ProjectSources projectSources,
                                         ApkDownloader apkDownloader,
                                         ApplicationState applicationState) {
        this.refreshBehavior = refreshBehavior;
        this.projectSources = projectSources;
        this.apkDownloader = apkDownloader;
        this.applicationState = applicationState;
        this.refreshBehavior.whenRefreshingDo(new Func0<Observable<?>>() {
            @Override
            public Observable<?> call() {
                return refresh();
            }
        });
    }

    private Observable<?> refresh() {
        return Observable.merge(
                fetchBuildDetails(),
                fetchBuildDetails().flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        return fetchBuildArtifacts();
                    }
                })
        );
    }

    private Observable<?> fetchBuildArtifacts() {
        getApkArtifactCollection().clear();
        return getApkArtifactCollection().fetchBuildArtifacts();
    }

    private Observable<?> fetchBuildDetails() {
        return buildResponse.fetchDetailsCachedOrNewWithDelay(projectSource,0, TimeUnit.MILLISECONDS);
    }

    @Override
    public RefreshBehavior getRefreshBehavior() {
        return refreshBehavior;
    }

    public void initWith(TeamcityBuildResponse teamcityBuildResponse) {
        buildResponse = teamcityBuildResponse;
    }

    public String getTitle() {
        String comment = buildResponse.lastChangeComment();
        if(TextUtils.isEmpty(comment)){
            return buildResponse.number;
        }
        return comment;
    }

    public TeamcityProjectSource getProjectSource() {
        if(projectSource == null){
            projectSource = (TeamcityProjectSource) projectSources.getSourceFor(buildResponse.getProjectOverView());
        }
        return projectSource;
    }

    public TeamcityBuildArtifactCollection getApkArtifactCollection(){
        if(artifactCollection == null){
            artifactCollection = new TeamcityBuildArtifactCollection(getProjectSource(), buildResponse);
        }
        return artifactCollection;
    }


    public rx.Subscription bindBuildInfo(View content) {
        TeamcityApkSourceViewHolder detailsViewHolder = new TeamcityApkSourceViewHolder(content, getProjectSource());
        detailsViewHolder.updateWith(buildResponse);
        return detailsViewHolder.getLoaderSubscription();
    }

    public RecyclerViewAdapterObserver createApkListAdapter() {
        adapter = new TeamcityApkArtifactAdapter(getApkArtifactCollection());
        return adapter;
    }

    private class TeamcityApkArtifactAdapter extends RecyclerViewAdapterObserver<TeamcityBuildArtifactsFile, ViewHolderOf<TeamcityBuildArtifactsFile>> {

        protected TeamcityApkArtifactAdapter(ObservableCollection<TeamcityBuildArtifactsFile> collection) {
            super(collection);
        }

        @Override
        public ViewHolderOf<TeamcityBuildArtifactsFile> onCreateViewHolder(ViewGroup parent, int viewType) {
            View apkView = LayoutInflater.from(parent.getContext()).inflate(R.layout.apk_item, parent, false);
            TeamcityBuildArtifactViewHolder viewHolder = new TeamcityBuildArtifactViewHolder(apkView);
            adapter.addSubscription(viewHolder.compositeSubscription);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolderOf<TeamcityBuildArtifactsFile> holder, int position) {
            TeamcityBuildArtifactsFile file = getItem(position);
            holder.updateWith(file);
        }
    }

    class TeamcityBuildArtifactViewHolder extends ViewHolderOf<TeamcityBuildArtifactsFile> {
        @InjectView(R.id.apkFileName) TextView fileName;
        @InjectView(R.id.apkFileAction) Button actionButton;
        private TeamcityBuildArtifactsFile currentItem;
        private Subscription apkActionSubscription;

        public TeamcityBuildArtifactViewHolder(View inflate) {
            super(inflate);
            ButterKnife.inject(this, itemView);
        }

        @OnClick(R.id.apkFileAction)
        public void onApkAction() {
            setApkActionSubscription(apkDownloader.downloadStart(getDownloadableApk()).subscribe(new Action1<DownloadAbleApkProgress>() {
                @Override
                public void call(DownloadAbleApkProgress downloadAbleApkProgress) {
                    LOG.trace("Download changed {}", downloadAbleApkProgress);
                    updateActionButtonText(downloadAbleApkProgress);
                    if (downloadAbleApkProgress.isCompletedWithSuccess() && applicationState.isInForeground()) {
                        InstallApkUtils.startInstall(downloadAbleApkProgress.getDownloadedApkFileUri(), itemView.getContext());
                        setApkActionSubscription(null);
                    }
                }
            }));
        }

        private DownloadAbleApk getDownloadableApk() {
            return projectSource.makeDownloadableApk(buildResponse, currentItem);
        }

        @Override
        public void updateWith(TeamcityBuildArtifactsFile apkSourceAt) {
            if(currentItem != apkSourceAt) {
                currentItem = apkSourceAt;
                fileName.setText(apkSourceAt.name);
                int nestingPadding = (int) (fileName.getResources().getDimension(R.dimen.nest_level_padding) * apkSourceAt.nestLevel);
                fileName.setPadding(nestingPadding, 0, 0, 0);
                if (apkSourceAt.isContent()) {
                    setApkActionSubscription(apkDownloader.getAndObserveDownloadState(getDownloadableApk()).subscribe(
                            new Action1<DownloadAbleApkProgress>() {
                                @Override
                                public void call(DownloadAbleApkProgress downloadAbleApkProgress) {
                                    updateActionButtonText(downloadAbleApkProgress);
                                }
                            }
                    ));
                    actionButton.setVisibility(View.VISIBLE);
                } else {
                    actionButton.setVisibility(View.GONE);
                }
            }
        }

        private void updateActionButtonText(DownloadAbleApkProgress downloadAbleApkProgress) {
            if (downloadAbleApkProgress.isCompletedWithSuccess()) {
                actionButton.setText(R.string.install_apk);
            } else {
                actionButton.setText(R.string.download);
            }
        }

        public void setApkActionSubscription(Subscription downloadSubscription) {
            if(this.apkActionSubscription != null){
                this.apkActionSubscription.unsubscribe();
                compositeSubscription.remove(apkActionSubscription);
                this.apkActionSubscription = null;
            }
            this.apkActionSubscription = downloadSubscription;
            if(downloadSubscription != null){
                compositeSubscription.add(downloadSubscription);
            }
        }
    }
}
