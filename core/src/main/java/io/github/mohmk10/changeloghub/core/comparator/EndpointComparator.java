package io.github.mohmk10.changeloghub.core.comparator;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.Endpoint;

import java.util.List;

public interface EndpointComparator {

    List<Change> compare(Endpoint oldEndpoint, Endpoint newEndpoint);
}
