/*
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

import static java.util.Collections.singletonList
import static java.util.Collections.singletonMap

class ProjectDetails implements ProjectDetailsAware {
    final String name
    final Map<String, List<VersionEntry>> releases
    final String latest

    ProjectDetails(String name, Map<String, List<VersionEntry>> releases, String latest) {
        this.name = name
        this.releases = releases
        this.latest = latest
    }

    static ProjectDetails createFrom(String name, VersionEntry versionEntry) {
        return new ProjectDetails(name, singletonMap(versionEntry.version, singletonList(versionEntry)),
            versionEntry.version)
    }

    @Override
    VersionEntry findVersion(Dependency dependency) {
        String fixedVersion = maybeFixVersion(dependency.version)
        if (releases.containsKey(fixedVersion)) {
            def versionEntry = releases[fixedVersion].find { it.packageType == dependency.type }
            if (versionEntry != null) {
                return versionEntry
            }
        }

        throw new RuntimeException("Unable to find $dependency")
    }

    @Override
    String maybeFixVersion(String version) {
        if (releases.containsKey(version)) {
            return version
        }

        if (releases.containsKey(version + '.0')) {
            return version + '.0'
        }

        if (releases.containsKey(version + '.0.0')) {
            return version + '.0.0'
        }

        throw new RuntimeException("Unable to find version $version for $name")
    }

    @Override
    String getLatestVersion() {
        return latest
    }

    List<String> getVersionInRange(VersionRange range, List<String> excluded) {
        String start = range.startVersion ?: '0'
        String end = range.endVersion ?: '999999'
        def sortedReleases = releases.keySet().sort { a, b -> VersionRange.compareVersions(a, b) }
        def matchingRange = []

        for (String release : sortedReleases) {
            if (excluded.contains(release)) {
                continue
            }
            if (!satisfies(release)) {
                // skip alpha/beta/release candidate versions
                continue
            }
            if (VersionRange.compareVersions(release, start) > 0 || (release == start && range.includeStart)) {
                matchingRange.add(release)
            }
            if (VersionRange.compareVersions(release, end) > 0 || (release == end && !range.includeEnd)) {
                break
            }
        }

        if (matchingRange.isEmpty()) {
            throw new RuntimeException("Unable to find ${name} in range ${range.startVersion}..${range.endVersion}")
        }

        return matchingRange
    }

    @Override
    boolean satisfies(String release) {
        return !(release ==~ /^.*\d(a|alpha|b|beta|rc|pre)\d*$/)
    }
}
