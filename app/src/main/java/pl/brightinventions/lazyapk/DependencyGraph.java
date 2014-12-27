package pl.brightinventions.lazyapk;

import java.util.ArrayList;
import java.util.List;

import dagger.ObjectGraph;

public class DependencyGraph {
    private static final DependencyGraph graph = new DependencyGraph();
    private ObjectGraph objectGraph;
    private List<Object> modules = new ArrayList<>();

    public static void inject(Object injectTarget) {
        graph.injectTarget(injectTarget);
    }

    private void injectTarget(Object injectTarget) {
        objectGraph().inject(injectTarget);
    }

    private synchronized ObjectGraph objectGraph() {
        if(objectGraph == null){
            objectGraph = ObjectGraph.create(Linq.toArray(modules));
        }
        return objectGraph;
    }

    public static void addModule(Object module) {
        graph.addGlobalModule(module);
    }

    private void addGlobalModule(Object module) {
        if(objectGraph != null){
            throw new IllegalStateException("Can't add module after graph has been created");
        }
        modules.add(module);
    }
}
