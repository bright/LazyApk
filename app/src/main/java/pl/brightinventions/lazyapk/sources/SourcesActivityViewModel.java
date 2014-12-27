package pl.brightinventions.lazyapk.sources;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.brightinventions.lazyapk.ProjectSource;
import pl.brightinventions.lazyapk.ProjectSourceFetchResult;
import pl.brightinventions.lazyapk.R;
import pl.brightinventions.lazyapk.RecyclerViewAdapterObserver;
import pl.brightinventions.lazyapk.teamcity.ObservableCollection;
import pl.brightinventions.lazyapk.teamcity.ViewHolderOf;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class SourcesActivityViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(SourcesActivityViewModel.class.getSimpleName());
    private static final String EMPTY = "";
    private final ProjectSourceFactory projectSourceFactory;
    private BehaviorSubject<Boolean> observeIsAddVisible = BehaviorSubject.create();
    private BehaviorSubject<Boolean> observeIsRemoveVisible = BehaviorSubject.create(false);
    private ArrayAdapter<CharSequence> sourceTypeAdapter;
    private BehaviorSubject<String> observeEditedSourceAddress = BehaviorSubject.create("http://");
    private BehaviorSubject<String> observeEditedSourceAddressError = BehaviorSubject.create();
    private BehaviorSubject<String> observeEditedSourceUserName = BehaviorSubject.create();
    private BehaviorSubject<String> observeEditedSourceUserNameError = BehaviorSubject.create();
    private BehaviorSubject<String> observeEditedSourcePassword = BehaviorSubject.create();
    private BehaviorSubject<String> observeEditedSourcePasswordError = BehaviorSubject.create();
    private ProjectSourceConfigurationCollection configurationCollection;
    private SourcesListAdapter adapter;
    private ProjectSourceConfiguration editedProjectSourceConfiguration;
    private boolean isNew;

    @Inject
    public SourcesActivityViewModel(ProjectSourceConfigurationCollection projectSources, ProjectSourceFactory projectSourceFactory) {
        this.configurationCollection = projectSources;
        this.projectSourceFactory = projectSourceFactory;
        if (isSourcesEmpty()) {
            onAddNewSource();
        } else {
            doneEditing();
        }
    }

    public boolean isSourcesEmpty() {
        return configurationCollection.size() == 0;
    }

    public void onAddNewSource() {
        setEditedProjectSource(new ProjectSourceConfiguration());
        clearErrors();
        isNew = true;
        observeIsAddVisible.onNext(false);
    }

    private void setEditedProjectSource(ProjectSourceConfiguration configuration) {
        LOG.trace("setEditedProjectSource {}", configuration);
        editedProjectSourceConfiguration = configuration;
        if (editedProjectSourceConfiguration != null) {
            observeEditedSourceAddress.onNext(editedProjectSourceConfiguration.getAddress());
            observeEditedSourceUserName.onNext(editedProjectSourceConfiguration.getUserName());
            observeEditedSourcePassword.onNext(editedProjectSourceConfiguration.getPassword());
        } else {
            observeEditedSourceAddress.onNext("http://");
            observeEditedSourceUserName.onNext(EMPTY);
            observeEditedSourcePassword.onNext(EMPTY);
        }
    }

    private void clearErrors() {
        observeEditedSourceAddressError.onNext(EMPTY);
        observeEditedSourceUserNameError.onNext(EMPTY);
        observeEditedSourcePasswordError.onNext(EMPTY);
    }

    public Observable<Boolean> observeIsAddVisible() {
        return observeIsAddVisible;
    }

    public Observable<Boolean> observeIsRemoveVisible() {
        return observeIsRemoveVisible;
    }

    public SpinnerAdapter createSourceTypeAdapter(Context context) {
        sourceTypeAdapter = ArrayAdapter.createFromResource(context, R.array.source_types, android.R.layout.simple_spinner_item);
        sourceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return sourceTypeAdapter;
    }

    public void onSourceTypeSelected(int position) {
    }

    public Observable<String> observeEditedSourceAddress() {
        return observeEditedSourceAddress;
    }

    public Observable<String> observeEditedSourceUserName() {
        return observeEditedSourceUserName;
    }

    public Observable<String> observeEditedSourcePassword() {
        return observeEditedSourcePassword;
    }

    public Observable<String> observeEditedSourceAddressError() {
        return observeEditedSourceAddressError;
    }

    public Observable<String> observeEditedSourceUserNameError() {
        return observeEditedSourceUserNameError;
    }

    public Observable<String> observeEditedSourcePasswordError() {
        return observeEditedSourcePasswordError;
    }

    public Observable<Void> onSave(Resources resources) {
        boolean clientSideValid = true;
        if (TextUtils.isEmpty(editedProjectSourceConfiguration.getAddress())) {
            observeEditedSourceAddressError.onNext(resources.getString(R.string.address_required));
            clientSideValid = false;
        } else if (!validUri(editedProjectSourceConfiguration.getAddress())) {
            observeEditedSourceAddressError.onNext(resources.getString(R.string.address_looks_invalid));
            clientSideValid = false;
        } else {
            observeEditedSourceAddressError.onNext(null);
        }
        if (TextUtils.isEmpty(editedProjectSourceConfiguration.getUserName())) {
            observeEditedSourceUserNameError.onNext(resources.getString(R.string.username_required));
            clientSideValid = false;
        } else {
            observeEditedSourceUserNameError.onNext(null);
        }
        if (TextUtils.isEmpty(editedProjectSourceConfiguration.getPassword())) {
            observeEditedSourcePasswordError.onNext(resources.getString(R.string.password_required));
            clientSideValid = false;
        } else {
            observeEditedSourcePasswordError.onNext(null);
        }

        if (!clientSideValid) {
            return Observable.empty();
        } else {
            ProjectSource projectSource = projectSourceFactory.create(editedProjectSourceConfiguration);
            Observable<ProjectSourceFetchResult> cache = projectSource.fetchProjects()
                    .cache()
                    .observeOn(AndroidSchedulers.mainThread());
            cache.subscribe(new Action1<ProjectSourceFetchResult>() {
                @Override
                public void call(ProjectSourceFetchResult projectSourceFetchResult) {
                    if (isNew) {
                        configurationCollection.add(editedProjectSourceConfiguration);
                    }
                    doneEditing();
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    observeEditedSourceAddressError.onNext(throwable.getLocalizedMessage());
                }
            });
            return cache.onErrorReturn(null).map(new Func1<ProjectSourceFetchResult, Void>() {
                @Override
                public Void call(ProjectSourceFetchResult projectSourceFetchResult) {
                    return null;
                }
            });
        }
    }

    private boolean validUri(String address) {
        try {
            return URI.create(address) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void onCancel() {
        doneEditing();
    }

    public RecyclerViewAdapterObserver createSourceListAdapter() {
        adapter = new SourcesListAdapter(configurationCollection);
        return adapter;
    }

    public void setEditedSourceAddress(CharSequence address) {
        if (editedProjectSourceConfiguration != null) {
            editedProjectSourceConfiguration.setAddress(coerceToString(address));
        }
    }

    private String coerceToString(CharSequence userName) {
        return userName != null ? userName.toString() : null;
    }

    public void setEditedSourceUsername(CharSequence userName) {
        if (editedProjectSourceConfiguration != null) {
            editedProjectSourceConfiguration.setUserName(coerceToString(userName));
        }
    }

    public void setEditedSourcePassword(CharSequence password) {
        if (editedProjectSourceConfiguration != null) {
            editedProjectSourceConfiguration.setPassword(coerceToString(password));
        }
    }

    private void doneEditing() {
        clearErrors();
        setEditedProjectSource(null);
        observeIsAddVisible.onNext(true);
        observeIsRemoveVisible.onNext(false);
        isNew = false;
    }

    private void beginEditing(ProjectSourceConfiguration currentItem) {
        isNew = false;
        clearErrors();
        setEditedProjectSource(currentItem);
        observeIsAddVisible.onNext(false);
        observeIsRemoveVisible.onNext(true);
    }

    public boolean handleBackPressed() {
        if(editedProjectSourceConfiguration != null){
            doneEditing();
            return true;
        }
        return false;
    }

    public void onRemoveEditedSource() {
        if(editedProjectSourceConfiguration != null){
            configurationCollection.remove(editedProjectSourceConfiguration);
            doneEditing();
        }
    }

    private class SourcesListAdapter extends RecyclerViewAdapterObserver<ProjectSourceConfiguration, ViewHolderOf<ProjectSourceConfiguration>> {

        protected SourcesListAdapter(ObservableCollection<ProjectSourceConfiguration> collection) {
            super(collection, 300, TimeUnit.MILLISECONDS);
        }

        @Override
        public ViewHolderOf<ProjectSourceConfiguration> onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_source_configuration_item, parent, false);
            return new ProjectSourceConfigurationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolderOf<ProjectSourceConfiguration> holder, int position) {
            LOG.trace("onBindViewHolder {} position {}", holder, position);
            holder.updateWith(getItem(position));
        }
    }

    class ProjectSourceConfigurationViewHolder extends ViewHolderOf<ProjectSourceConfiguration> {
        @InjectView(R.id.sourceAddress) TextView sourceAddress;

        private ProjectSourceConfiguration currentItem;

        public ProjectSourceConfigurationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beginEditingCurrentItem();
                }
            });
        }

        @OnClick(R.id.sourceEdit)
        public void beginEditingCurrentItem() {
            if(currentItem != null){
                beginEditing(currentItem);
            }
        }

        @Override
        public void updateWith(ProjectSourceConfiguration apkSourceAt) {
            currentItem = apkSourceAt;
            sourceAddress.setText(apkSourceAt.getAddress());
        }
        @OnClick(R.id.sourceEdit)
        public void onEdit() {
        }

    }
}
