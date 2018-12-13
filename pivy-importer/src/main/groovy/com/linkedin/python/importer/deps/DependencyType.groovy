package com.linkedin.python.importer.deps

enum DependencyType {
    SOURCE_DISTRIBUTION("sdist", "pypi"),
    WHEEL("bdist_wheel", "wheel"),
    WININST("bdist_wininst", "wininst"),
    EGG("bdist_egg", "egg"),
    RPM("bdist_rpm", "rpm")

    final String pythonType
    final String org

    DependencyType(String pythonType, String org) {
        this.pythonType = pythonType
        this.org = org
    }

    static DependencyType forFile(File artifact) {
        def name = artifact.name
        if (name.endsWith("whl")) {
            return WHEEL
        }
        if (name.contains(".tar.") || name.endsWith(".tgz") || name.endsWith(".zip")) {
            return SOURCE_DISTRIBUTION
        }
        throw new RuntimeException("Unsupported dist type for $name")
    }

    static DependencyType forPythonType(String type) {
        for (dependencyType in values()) {
            if (dependencyType.pythonType == type) {
                return dependencyType
            }
        }
        throw new RuntimeException("Type $type not supported")
    }
}
