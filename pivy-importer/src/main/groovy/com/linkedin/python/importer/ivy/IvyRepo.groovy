package com.linkedin.python.importer.ivy

import com.linkedin.python.importer.deps.Dependency
import com.linkedin.python.importer.pypi.VersionEntry

import java.nio.file.Paths

class IvyRepo {
    final File directory
    final String repoPath

    IvyRepo(File directory) {
        this.directory = directory
        repoPath = directory.absolutePath
    }

    File acquireArtifactDirectory(Dependency dependency) {
        def type = dependency.type
        def path = Paths.get(repoPath, type.org, dependency.moduleName, dependency.version, dependency.classifier)
        def artifactDirectory = path.toFile()
        if (!artifactDirectory.exists()) {
            artifactDirectory.mkdirs()
        }
        return artifactDirectory
    }

    void writeIvyMetadata(Dependency dependency, VersionEntry matchingVersion, Map<String, List<String>> deps) {
        new IvyFileWriter(dependency, [matchingVersion]).writeIvyFile(acquireArtifactDirectory(dependency), deps)
    }
}
