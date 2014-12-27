package pl.brightinventions.lazyapk;

import android.content.Context;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import rx.Observer;
import rx.functions.Action1;

public class Toaster implements Observer {
    private static final Logger LOG = LoggerFactory.getLogger(Toaster.class.getSimpleName());
    private final Context context;

    @Inject
    public Toaster(@AppContext Context context) {
        this.context = context;
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        LOG.error("Error handling observable", e);
        String message = e.getMessage();
        if(!BuildConfig.DEBUG){
            message  = e.getLocalizedMessage();
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNext(Object o) {

    }

    @SuppressWarnings("unchecked")
    public <T> Observer<T> subscriber() {
        return (Observer<T>) this;
    }

    public void showError(int stringResId) {
        showMessage(stringResId, Toast.LENGTH_LONG);
    }

    private void showMessage(int stringResId, int length) {
        Toast.makeText(context, context.getText(stringResId), length).show();
    }
}
