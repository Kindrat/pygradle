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
package com.linkedin.python.importer.deps

import com.linkedin.python.importer.distribution.PackageFactory
import com.linkedin.python.importer.distribution.SourceDistPackage
import com.linkedin.python.importer.ivy.IvyFileWriter
import com.linkedin.python.importer.ivy.IvyRepo
import com.linkedin.python.importer.pypi.cache.ApiCache
import com.linkedin.python.importer.pypi.client.Client
import groovy.util.logging.Slf4j

import static com.linkedin.python.importer.deps.Dependency.parseFrom
import static com.linkedin.python.importer.deps.DependencyType.*

@Slf4j
class SdistDownloader extends DependencyDownloader {
    private final IvyRepo localIvyRepo
    private final Client pypiClient
    private final ApiCache cache
    private final PackageFactory packageFactory

    SdistDownloader(String project, IvyRepo localIvyRepo, ApiCache cache, Client pypiClient, PackageFactory packageFactory) {
        super(project)
        this.cache = cache
        this.localIvyRepo = localIvyRepo
        this.pypiClient = pypiClient
        this.packageFactory = packageFactory
    }

    @Override
    List<String> download(boolean latestVersions, boolean allowPreReleases, boolean fetchExtras) {
        def dependency = parseFrom(project)
        def projectDetails = cache.getDetails(dependency)
        def matchingVersion = projectDetails.findVersion(dependency)

        // make sure the module name has the right letter case and dash or underscore as PyPI
        def name = getActualModuleNameFromFilename(matchingVersion.filename, matchingVersion.version)
        log.info("Pulling in $dependency")

        def destDir = localIvyRepo.acquireArtifactDirectory(dependency)
        def artifact = pypiClient.downloadArtifact(destDir, matchingVersion.url)

        def packageDependencies = packageFactory.createPackage(SOURCE_DISTRIBUTION, name, matchingVersion.version, artifact)
            .getDependencies(latestVersions, allowPreReleases, fetchExtras)

        localIvyRepo.writeIvyMetadata(dependency, matchingVersion, packageDependencies)

        List<String> dependencies = new ArrayList<>()
        packageDependencies.values().each { list -> dependencies.addAll(list)}
        return dependencies
    }
}
