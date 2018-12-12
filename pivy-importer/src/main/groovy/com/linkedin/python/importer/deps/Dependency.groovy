package com.linkedin.python.importer.deps

import static com.linkedin.python.importer.deps.DependencyType.*

class Dependency {
    final String moduleName
    final String version
    final DependencyType type
    final String classifier

    Dependency(String moduleName, String version, DependencyType type, String classifier) {
        this.moduleName = moduleName
        this.version = version
        this.type = type
        this.classifier = classifier
    }

    static Dependency parseFrom(String rawValue) {
        def parts = rawValue.split(":")
        if (parts.length == 2) {
            return new Dependency(parts[0], parts[1], SOURCE_DISTRIBUTION, '')
        } else if (parts.length == 3) {
            return new Dependency(parts[0], parts[1], WHEEL, parts[2])
        }
        throw new RuntimeException("Unknown dependency type $rawValue")
    }


    @Override
    String toString() {
        return "Dependency{" +
            "moduleName='" + moduleName + '\'' +
            ", version='" + version + '\'' +
            ", type=" + type +
            ", classifier='" + classifier + '\'' +
            '}';
    }
}
