package pl.brightinventions.lazyapk.sources;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import pl.brightinventions.lazyapk.ObserveHelper;
import pl.brightinventions.lazyapk.R;
import pl.brightinventions.lazyapk.RecyclerViewAdapterObserver;
import pl.brightinventions.lazyapk.ToolbarSetup;
import pl.brightinventions.lazyapk.ViewUtils;
import pl.brightinventions.lazyapk.drawable.Icons;
import pl.brightinventions.lazyapk.setup.ActivitySetup;
import pl.brightinventions.lazyapk.setup.RecyclerViewSetup;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SourcesActivity extends ActionBarActivity {
    @InjectView(R.id.loader) ProgressBar loader;
    @InjectView(R.id.sourcesList) RecyclerView sourcesList;
    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.newSourceForm) View newSourceForm;
    @InjectView(R.id.addNewSource) TextView addNewSource;
    @InjectView(R.id.removeEditedSource) TextView removeEditedSource;
    @InjectView(R.id.save) Button save;
    @InjectView(R.id.cancel) Button cancel;
    @InjectView(R.id.newSourceType) Spinner editedSourceType;
    @InjectView(R.id.editedSourceAddress) TextView editedSourceAddress;
    @InjectView(R.id.editedSourceAddressError) TextView editedSourceAddressError;
    @InjectView(R.id.editedSourceUserName) TextView editedSourceUserName;
    @InjectView(R.id.editedSourceUserNameError) TextView editedSourceUserNameError;
    @InjectView(R.id.editedSourcePassword) TextView editedSourcePassword;
    @InjectView(R.id.editedSourcePasswordError) TextView editedSourcePasswordError;

    @Inject
    SourcesActivityViewModel viewModel;

    private CompositeSubscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetup.setup(this, R.layout.sources);

        Icons.left(addNewSource, Icons.plus(this));
        Icons.left(removeEditedSource, Icons.trash(this));

        ToolbarSetup.asSupportWithUp(toolbar, getString(R.string.sources), this);

        subscription = new CompositeSubscription();

        RecyclerViewAdapterObserver adapter = viewModel.createSourceListAdapter();
        RecyclerViewSetup.linearWithDivider(sourcesList, adapter);

        subscription.add(adapter.getSubscription());

        subscription.add(ObserveHelper.bindVisibility(viewModel.observeIsAddVisible(), addNewSource));
        subscription.add(ObserveHelper.bindVisibility(viewModel.observeIsRemoveVisible(), removeEditedSource));

        subscription.add(ObserveHelper.bindText(viewModel.observeEditedSourceAddress(), editedSourceAddress) );
        subscription.add(ObserveHelper.bindText(viewModel.observeEditedSourceAddressError(), editedSourceAddressError) );
        subscription.add(ObserveHelper.bindText(viewModel.observeEditedSourceUserName(), editedSourceUserName) );
        subscription.add(ObserveHelper.bindText(viewModel.observeEditedSourceUserNameError(), editedSourceUserNameError) );
        subscription.add(ObserveHelper.bindText(viewModel.observeEditedSourcePassword(), editedSourcePassword) );
        subscription.add(ObserveHelper.bindText(viewModel.observeEditedSourcePasswordError(), editedSourcePasswordError) );
        newSourceForm.setTranslationY(ViewUtils.getDisplayHeight(this));
        subscription.add(viewModel.observeIsAddVisible().subscribe(new Action1<Boolean>() {
            @Override
            public void call(final Boolean newSourceInvisible) {
                ViewUtils.clearFocusAndKeyboard(newSourceForm);
                int displayHeight = ViewUtils.getDisplayHeight(SourcesActivity.this);
                newSourceForm.animate().translationY(newSourceInvisible ? displayHeight : 0)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                        .setInterpolator(newSourceInvisible ? new AccelerateInterpolator() : new DecelerateInterpolator())
                        .start();
            }
        }));
        editedSourceType.setAdapter(viewModel.createSourceTypeAdapter(this));
        editedSourceType.setSelection(0);
    }

    @OnItemSelected(R.id.newSourceType)
    public void onSourceTypeSelected(int position){
        viewModel.onSourceTypeSelected(position);
    }

    @OnClick(R.id.addNewSource)
    public void onAddNewSource(){
        viewModel.onAddNewSource();
    }

    @OnClick(R.id.save)
    public void onSave(){
        ViewUtils.clearFocusAndKeyboard(newSourceForm);
        subscription.add(ObserveHelper.showProgressUntilComplete(loader, viewModel.onSave(getResources())));
    }

    @OnTextChanged(R.id.editedSourceAddress)
    public void onAddressChanged(CharSequence address){
        viewModel.setEditedSourceAddress(address);
    }
    @OnTextChanged(R.id.editedSourceUserName)
    public void onUserNameChanged(CharSequence userName){
        viewModel.setEditedSourceUsername(userName);
    }
    @OnTextChanged(R.id.editedSourcePassword)
    public void onPasswordChanged(CharSequence password){
        viewModel.setEditedSourcePassword(password);
    }

    @OnClick(R.id.cancel)
    public void onCancel(){
        viewModel.onCancel();
    }

    @OnClick(R.id.removeEditedSource)
    public void onRemoveEditedSource(){viewModel.onRemoveEditedSource(); }

    @Override
    public void onBackPressed() {
        if(!viewModel.handleBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if(subscription != null){
            subscription.unsubscribe();
            subscription = null;
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, SourcesActivity.class);
        return intent;
    }
}
