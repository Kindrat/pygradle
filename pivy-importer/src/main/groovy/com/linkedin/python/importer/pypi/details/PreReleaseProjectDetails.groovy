/**
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.python.importer.pypi.details

import com.linkedin.python.importer.deps.Dependency
import com.linkedin.python.importer.pypi.VersionEntry
import com.linkedin.python.importer.pypi.VersionRange

class PreReleaseProjectDetails implements ProjectDetailsAware {
    private final ProjectDetailsAware delegate

    PreReleaseProjectDetails(ProjectDetailsAware delegate) {
        this.delegate = delegate
    }

    @Override
    VersionEntry findVersion(Dependency dependency) {
        return delegate.findVersion(dependency)
    }

    @Override
    List<String> getVersionInRange(VersionRange range, List<String> excluded) {
        return delegate.getVersionInRange(range, excluded)
    }

    @Override
    String getLatestVersion() {
        return delegate.getLatestVersion()
    }

    @Override
    String getName() {
        return delegate.getName()
    }

    @Override
    boolean satisfies(String version) {
        return true
    }

    @Override
    String maybeFixVersion(String version) {
        return delegate.maybeFixVersion(version)
    }
}
