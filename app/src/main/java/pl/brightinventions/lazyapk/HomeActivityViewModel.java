package pl.brightinventions.lazyapk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

public class HomeActivityViewModel implements RefreshSource {

    private final ProjectSources projectSources;
    ArrayList<ProjectOverview> projectOverviews = new ArrayList<>();
    private RecyclerView.Adapter mainListAdapter;
    private PublishSubject<Boolean> observeRefreshing = PublishSubject.create();
    private PublishSubject<Boolean> observeEmptyProjects = PublishSubject.create();
    private PublishSubject<ProjectOverview> openProjectDetails = PublishSubject.create();
    private RefreshBehavior refreshBehavior;

    @Inject
    public HomeActivityViewModel(ProjectSources projectSources, RefreshBehavior refreshBehavior) {
        this.projectSources = projectSources;
        this.refreshBehavior = refreshBehavior;
        this.refreshBehavior.whenRefreshingDo(new Func0<Observable<?>>() {
            @Override
            public Observable<?> call() {
                return fetchAllProjects();
            }
        });
    }

    public Observable<?> fetchAllProjects() {
        return projectSources.fetchProjects()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<AllProjectsFetchResult>() {
                    @Override
                    public void call(AllProjectsFetchResult value) {
                        projectOverviews = Linq.toList(value.getAllProjects());
                        mainListAdapter.notifyDataSetChanged();
                        observeRefreshing.onNext(false);
                        observeEmptyProjects.onNext(projectOverviews.isEmpty());
                    }
                });
    }

    public Observable<Boolean> observeEmptyProjects() {
        return observeEmptyProjects;
    }

    public Observable<ProjectOverview> openProjectDetails() {
        return openProjectDetails;
    }

    public RecyclerView.Adapter createProjectListAdapter(final Context context) {
        return mainListAdapter = new RecyclerView.Adapter<ProjectViewHolder>() {

            @Override
            public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ProjectViewHolder(LayoutInflater.from(context).inflate(R.layout.project_list_item, parent, false));
            }

            @Override
            public void onBindViewHolder(ProjectViewHolder holder, int position) {
                holder.updateWith(projectOverviews.get(position));
            }

            @Override
            public int getItemCount() {
                return projectOverviews.size();
            }
        };
    }

    public void refreshIfEmpty() {
        if (projectOverviews.isEmpty()) {
            getRefreshBehavior().refreshIfNotRefreshing();
        }
    }

    @Override
    public RefreshBehavior getRefreshBehavior() {
        return refreshBehavior;
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.projectName) TextView projectName;
        private ProjectOverview item;

        public ProjectViewHolder(View inflate) {
            super(inflate);
            ButterKnife.inject(this, inflate);
            inflate.setOnClickListener(this);
        }

        public void updateWith(ProjectOverview projectOverview) {
            projectName.setText(projectOverview.getName());
            item = projectOverview;
        }

        @Override
        public void onClick(View v) {
            openProjectDetails.onNext(item);
        }
    }
}
