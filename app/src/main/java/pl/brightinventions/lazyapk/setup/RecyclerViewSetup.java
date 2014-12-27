package pl.brightinventions.lazyapk.setup;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import pl.brightinventions.lazyapk.DividerItemDecoration;

public class RecyclerViewSetup {
    public static void linearWithDivider(RecyclerView mainList, RecyclerView.Adapter adapter) {
        mainList.addItemDecoration(new DividerItemDecoration(mainList.getContext(), LinearLayoutManager.VERTICAL));
        mainList.setAdapter(adapter);
        mainList.setLayoutManager(new LinearLayoutManager(mainList.getContext(), LinearLayoutManager.VERTICAL, false));
    }
}
