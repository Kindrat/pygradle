package com.linkedin.python.importer.pypi.client

import com.linkedin.python.importer.pypi.details.ProjectDetailsAware

interface Client {
    File downloadArtifact(File destDir, String url)

    ProjectDetailsAware downloadMetadata(String dependency)
}
