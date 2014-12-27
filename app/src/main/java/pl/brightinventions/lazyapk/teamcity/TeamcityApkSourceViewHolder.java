package pl.brightinventions.lazyapk.teamcity;

import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.brightinventions.lazyapk.ClickableItemOf;
import pl.brightinventions.lazyapk.ProjectApkSource;
import pl.brightinventions.lazyapk.ProjectSource;
import pl.brightinventions.lazyapk.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

class TeamcityApkSourceViewHolder extends MasterDetailsViewHolderOf<ProjectApkSource> implements ClickableItemOf<ProjectApkSource> {
    private static final Logger LOG = LoggerFactory.getLogger(TeamcityApkSourceViewHolder.class.getSimpleName());
    private final ProjectSource projectSource;
    private final View.OnClickListener openApkListener;
    @InjectView(R.id.buildNumber) TextView buildNumber;
    @InjectView(R.id.buildType) TextView buildType;
    @InjectView(R.id.buildWhen) TextView buildWhen;
    @InjectView(R.id.buildComment) TextView buildComment;
    PublishSubject<ProjectApkSource> observeOnClick = PublishSubject.create();
    private ProjectApkSource currentApkSource;

    public TeamcityApkSourceViewHolder(View inflate, ProjectSource projectSource) {
        super(inflate);
        this.projectSource = projectSource;
        ButterKnife.inject(this, inflate);
        openApkListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentApkSource != null) {
                    observeOnClick.onNext(currentApkSource);
                } else {
                    LOG.warn("Clicked view holder without current apk source");
                }
            }
        };
        inflate.setOnClickListener(openApkListener);
    }

    @OnClick(R.id.buildComment)
    public void onCommentClick(View v){
        openApkListener.onClick(v);
    }
    @Override
    public void updateWith(ProjectApkSource apkSourceAt) {
        currentApkSource = apkSourceAt;
        updateView((TeamcityBuildResponse) apkSourceAt);
        setDetailsLoadSubscription(
                apkSourceAt.fetchDetailsCachedOrNewWithDelay(projectSource, 500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ProjectApkSource>() {
                            @Override
                            public void call(ProjectApkSource projectApkSource) {
                                updateView((TeamcityBuildResponse) projectApkSource);
                            }
                        }));
    }

    private void updateView(TeamcityBuildResponse apkSourceAt) {
        buildNumber.setText(apkSourceAt.number);
        buildType.setText(apkSourceAt.buildTypeId);
        Date finishDate = apkSourceAt.finishDate;
        if (finishDate != null) {
            CharSequence relativeDateTimeString = DateUtils.getRelativeDateTimeString(
                    buildWhen.getContext(),
                    finishDate.getTime(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE);
            buildWhen.setText(relativeDateTimeString);
        } else {
            buildWhen.setText("...");
        }
        if (apkSourceAt.hasLastChange()) {
            buildComment.setText(apkSourceAt.lastChanges.first().comment);
            buildComment.setMovementMethod(new ScrollingMovementMethod());
        } else {
            buildComment.setText(null);
        }
    }

    @Override
    public Observable<ProjectApkSource> onClick() {
        return observeOnClick;
    }
}
