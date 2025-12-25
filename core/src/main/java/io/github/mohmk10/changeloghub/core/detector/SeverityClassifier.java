package io.github.mohmk10.changeloghub.core.detector;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.Severity;

public interface SeverityClassifier {

    Severity classify(Change change);
}
