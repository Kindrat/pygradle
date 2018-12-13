package com.linkedin.python.importer.pypi.client

import com.linkedin.python.importer.deps.DependencyType
import com.linkedin.python.importer.pypi.VersionEntry
import com.linkedin.python.importer.pypi.details.ProjectDetails
import com.linkedin.python.importer.pypi.details.ProjectDetailsAware
import groovy.util.logging.Slf4j

@Slf4j
class LocalArtifactClient implements Client {
    private final Client delegate
    private final Map<String, ProjectDetailsAware> localCache

    LocalArtifactClient(Client delegate, File localRepo) {
        this.delegate = delegate
        this.localCache = parseLocalRepoFiles(localRepo)
    }

    @Override
    File downloadArtifact(String url) {
        def file = new File(url)
        if (file.exists()) {
            return file
        }
        return delegate.downloadArtifact(url)
    }

    @Override
    ProjectDetailsAware downloadMetadata(String dependency) {
        return localCache.getOrDefault(dependency, delegate.downloadMetadata(dependency))
    }

    Map<String, ProjectDetailsAware> parseLocalRepoFiles(File localCache) {
        if (localCache.isFile()) {
            ProjectDetailsAware projectDetails = parseFile(localCache)
            Objects.requireNonNull(projectDetails, "Single repo file should be valid dist")
            return Collections.singletonMap(projectDetails.name, projectDetails)
        }
        Map<String, ProjectDetailsAware> cache = new HashMap<>()
        localCache.listFiles().each { it ->
            ProjectDetailsAware projectDetails = parseFile(it)
            if (projectDetails != null) {
                cache.put(projectDetails.name, projectDetails)
            }
        }
        return Collections.unmodifiableMap(cache)
    }

    ProjectDetailsAware parseFile(File artifact) {
        String name = ""
        String version = ""
        if (artifact.name.endsWith("whl")) {
            def parts = artifact.name.replace(".whl", "").split("-")
            name = parts[0]
            version = parts[1]
        } else if (artifact.name.endsWith("tgz")) {
            def noExtensionName = artifact.name.replace(".tgz", "")
            name = noExtensionName.substring(0, noExtensionName.lastIndexOf("-"))
            version = noExtensionName.substring(noExtensionName.lastIndexOf("-") + 1, noExtensionName.length())
        } else if (artifact.name.contains(".tar.")) {
            def noExtensionName = artifact.name.substring(0, artifact.name.indexOf(".tar."))
            version = noExtensionName.substring(noExtensionName.lastIndexOf("-") + 1, noExtensionName.length())
            name = noExtensionName.substring(0, noExtensionName.lastIndexOf("-"))
        } else {
            return null
        }

        VersionEntry versionEntry = new VersionEntry(artifact.getAbsolutePath(), DependencyType.forFile(artifact),
            artifact.getName(), version)
        return ProjectDetails.createFrom(name, versionEntry)
    }
}
