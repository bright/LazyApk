package pl.brightinventions.lazyapk;

import android.app.Activity;
import android.content.Intent;

import java.io.Serializable;


public class RequiredIntentValueSupplier<T extends Serializable> implements SerializableSupplier<T> {
    private final Activity activity;
    private final String key;
    private T cachedValue;

    public RequiredIntentValueSupplier(Activity activity, String key) {
        this.activity = activity;
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        if(cachedValue != null){
            return cachedValue;
        }
        Intent intent = activity.getIntent();
        if(intent == null){
            throw new IllegalStateException(String.format("Required intent value for key %s not found in activity %s", key, activity));
        }
        Serializable extra = intent.getSerializableExtra(key);
        if(extra == null){
            throw new IllegalStateException(String.format("Required intent value for key %s not found in intent %s", key, intent));
        }
        cachedValue = (T) extra;
        return cachedValue;
    }
}
