package com.linkedin.python.importer.distribution;

import com.linkedin.python.importer.deps.DependencySubstitution;
import com.linkedin.python.importer.deps.DependencyType;
import com.linkedin.python.importer.pypi.cache.ApiCache;

import java.io.File;

public class PackageFactory {
    private final ApiCache cache;
    private final DependencySubstitution replacements;

    public PackageFactory(ApiCache cache, DependencySubstitution replacements) {
        this.cache = cache;
        this.replacements = replacements;
    }

    public PythonPackage createPackage(DependencyType type, String moduleName, String version, File file) {
        switch (type) {
            case SOURCE_DISTRIBUTION:
                return new SourceDistPackage(moduleName, version, file, cache, replacements);
            case WHEEL:
                return new WheelsPackage(moduleName, version, file, cache, replacements);
        }
        throw new NullPointerException("Package type not provided : " + moduleName);
    }
}
