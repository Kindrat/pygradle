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
package com.linkedin.python.importer.pypi

class PreReleaseProjectDetails implements ProjectDetailsAware {
    private final ProjectDetailsAware delegate

    PreReleaseProjectDetails(ProjectDetailsAware delegate) {
        this.delegate = delegate
    }

    @Override
    List<VersionEntry> findVersion(String version) {
        return delegate.findVersion(version)
    }

    @Override
    String maybeFixVersion(String version) {
        return delegate.maybeFixVersion(version)
    }

    @Override
    String getHighestVersionInRange(VersionRange range, List<String> excluded) {
        return delegate.getHighestVersionInRange(range, excluded)
    }

    @Override
    String getLowestVersionInRange(VersionRange range, List<String> excluded) {
        return delegate.getLowestVersionInRange(range, excluded)
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
    boolean hasVersion(String version) {
        return delegate.hasVersion(version)
    }

    @Override
    boolean satisfies(String version) {
        return true
    }
}
