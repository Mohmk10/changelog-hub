package io.github.mohmk10.changeloghub.core.comparator;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;

public interface ApiComparator {

    Changelog compare(ApiSpec oldSpec, ApiSpec newSpec);
}
