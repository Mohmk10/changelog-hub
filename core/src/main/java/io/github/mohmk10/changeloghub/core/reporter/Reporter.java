package io.github.mohmk10.changeloghub.core.reporter;

import io.github.mohmk10.changeloghub.core.model.Changelog;

import java.nio.file.Path;

public interface Reporter {

    String report(Changelog changelog);

    void reportToFile(Changelog changelog, Path outputPath);
}
