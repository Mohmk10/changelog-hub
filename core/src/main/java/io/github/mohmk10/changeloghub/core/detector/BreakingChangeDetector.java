package io.github.mohmk10.changeloghub.core.detector;

import io.github.mohmk10.changeloghub.core.model.BreakingChange;
import io.github.mohmk10.changeloghub.core.model.Change;

import java.util.List;

public interface BreakingChangeDetector {

    List<BreakingChange> detect(List<Change> changes);

    boolean isBreaking(Change change);
}
