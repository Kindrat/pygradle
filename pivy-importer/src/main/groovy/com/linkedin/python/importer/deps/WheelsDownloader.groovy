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
import com.linkedin.python.importer.ivy.IvyRepo
import com.linkedin.python.importer.pypi.cache.ApiCache
import com.linkedin.python.importer.pypi.client.Client
import groovy.util.logging.Slf4j

import static com.linkedin.python.importer.deps.DependencyType.WHEEL
import static org.apache.commons.io.FileUtils.copyFileToDirectory
import static org.apache.commons.io.FileUtils.moveDirectory

@Slf4j
class WheelsDownloader extends DependencyDownloader {
    final Client pypiClient
    final IvyRepo localIvyRepo
    private final ApiCache cache
    private final PackageFactory packageFactory

    WheelsDownloader(String project, IvyRepo localIvyRepo, ApiCache cache, Client pypiClient,
                     PackageFactory packageFactory) {
        super(project)
        this.packageFactory = packageFactory
        this.cache = cache
        this.localIvyRepo = localIvyRepo
        this.pypiClient = pypiClient
    }

    /**
     * The module names in Wheel artifact names are using "_" to replace "-", eg., python-submit,
     * its wheel artifact is python_subunit-1.3.0-py2.py3-none-any.whl.
     * @param name
     * @return
     */

    @Override
    List<String> download(boolean latestVersions, boolean allowPreReleases, boolean fetchExtras) {
        def dependency = Dependency.parseFrom(project)
        def projectDetails = cache.getDetails(dependency)
        def matchingVersion = projectDetails.findVersion(dependency)

        // make sure the module name has the same letter case as PyPI
        def name = getActualModuleNameFromFilename(matchingVersion.filename, matchingVersion.version)
        log.info("Pulling in $dependency")

        def destDir = localIvyRepo.acquireArtifactDirectory(dependency)
        def artifact = pypiClient.downloadArtifact(matchingVersion.url)
        copyFileToDirectory(artifact, destDir)

        def packageDependencies = packageFactory.createPackage(WHEEL, name, matchingVersion.version, artifact)
            .getDependencies(latestVersions, allowPreReleases, fetchExtras)

        log.debug("The dependencies of package $project: is ${packageDependencies.toString()}")

        localIvyRepo.writeIvyMetadata(dependency, matchingVersion, packageDependencies)

        List<String> dependencies = new ArrayList<>()
        packageDependencies.values().each { list -> dependencies.addAll(list) }
        return dependencies
    }
}
