package pl.brightinventions.lazyapk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.functions.Func1;

public class FetchListResult<T> {
    protected List<T> results = new ArrayList<>();
    public void addResult(T result){
        addResults(Linq.from(result));
    }
    public void addResults(Iterable<? extends T> resultsToAdd){
        Linq.addAll(this.results, resultsToAdd);
    }

    public Iterable<T> getResults(){
        return results;
    }

    public <TOther extends Comparable<TOther>> void sortBy(Func1<T,TOther> func1) {
        Collections.sort(results, ByPropertyComparator.of(func1));
    }

    public int getResultsCount() {
        return results.size();
    }
}
