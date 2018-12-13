package com.linkedin.python.importer.pypi.client

import com.linkedin.python.importer.pypi.details.ProjectDetailsAware

interface Client {
    File downloadArtifact(String url)

    ProjectDetailsAware downloadMetadata(String dependency)
}
