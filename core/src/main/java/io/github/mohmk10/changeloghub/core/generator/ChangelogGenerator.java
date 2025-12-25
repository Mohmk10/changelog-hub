package io.github.mohmk10.changeloghub.core.generator;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;

public interface ChangelogGenerator {

    Changelog generate(ApiSpec oldSpec, ApiSpec newSpec);
}
