package pl.brightinventions.lazyapk;

import java.io.Serializable;

public interface ProjectOverview extends Serializable {
    String getId();
    String getName();
    String getDescription();
}
