package pl.brightinventions.lazyapk;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import pl.brightinventions.lazyapk.drawable.Icons;

public class ToolbarSetup {
    public static void asSupportWithUp(Toolbar toolbar, String name, final ActionBarActivity activity) {
        toolbar.setNavigationIcon(Icons.navigationUp(activity));
        toolbar.setTitle(name);
        activity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(Icons.navigationUp(activity));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
    }
}
