package pl.brightinventions.lazyapk.sources;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import pl.brightinventions.lazyapk.AppContext;
import pl.brightinventions.lazyapk.teamcity.ObservableCollection;
import rx.Observable;
import rx.subjects.PublishSubject;

@Singleton
public class ProjectSourceConfigurationCollection implements ObservableCollection<ProjectSourceConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectSourceConfigurationCollection.class.getSimpleName());
    private final ArrayList<ProjectSourceConfiguration> configurations;
    private final SharedPreferences preferences;
    private PublishSubject<Pair<ProjectSourceConfiguration, Integer>> itemInsertedAt = PublishSubject.create();
    private PublishSubject<Pair<Integer, Integer>> itemsAtRangeRemoved = PublishSubject.create();
    private Type configurationListType = new TypeToken<ArrayList<ProjectSourceConfiguration>>(){}.getType();

    @Inject
    public ProjectSourceConfigurationCollection(@AppContext Context context) {
        preferences = context.getSharedPreferences("ConfigurationCollection", Context.MODE_PRIVATE);
        configurations = new ArrayList<>(readConfigurations());
    }

    private ArrayList<ProjectSourceConfiguration> readConfigurations() {
        LOG.trace("Start - read configuration");
        ArrayList<ProjectSourceConfiguration> configurationList = new Gson().fromJson(preferences.getString("configurationList", "[]"), configurationListType);
        LOG.trace("End - read configuration");
        return configurationList;
    }

    @Override
    public int size() {
        return configurations.size();
    }

    @Override
    public ProjectSourceConfiguration get(int position) {
        return configurations.get(position);
    }

    @Override
    public Observable<Pair<ProjectSourceConfiguration, Integer>> itemInsertedAt() {
        return itemInsertedAt;
    }

    @Override
    public Observable<Pair<Integer, Integer>> itemsAtRangeRemoved() {
        return itemsAtRangeRemoved;
    }

    @Override
    public Iterator<ProjectSourceConfiguration> iterator() {
        return new ArrayList<>(configurations).iterator();
    }

    public void add(ProjectSourceConfiguration configuration) {
        if(configuration == null){
            throw new IllegalArgumentException("Configuration must not be null");
        }
        configurations.add(0, configuration);
        persistConfigurations();
        itemInsertedAt.onNext(Pair.create(configuration,0));
    }

    public void remove(ProjectSourceConfiguration configuration) {
        int removeAtIndex = configurations.indexOf(configuration);
        if(removeAtIndex != -1){
            configurations.remove(removeAtIndex);
            persistConfigurations();
            itemsAtRangeRemoved.onNext(Pair.create(removeAtIndex,1));
        }
    }

    private void persistConfigurations() {
        LOG.trace("Start - write configuration");
        preferences.edit().putString("configurationList", new Gson().toJson(configurations)).apply();
        LOG.trace("End - write configuration");
    }
}
