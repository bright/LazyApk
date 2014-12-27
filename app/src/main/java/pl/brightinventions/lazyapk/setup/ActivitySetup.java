package pl.brightinventions.lazyapk.setup;

import android.app.Activity;

import butterknife.ButterKnife;
import pl.brightinventions.lazyapk.DependencyGraph;

public class ActivitySetup {
    public static <TActivity extends Activity> void setup(TActivity homeActivity, int layout) {
        homeActivity.setContentView(layout);
        DependencyGraph.inject(homeActivity);
        ButterKnife.inject(homeActivity);
    }
}
