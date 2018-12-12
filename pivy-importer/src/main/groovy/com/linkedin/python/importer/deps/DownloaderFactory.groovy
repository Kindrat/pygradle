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
package com.linkedin.python.importer.deps

import com.linkedin.python.importer.distribution.PackageFactory
import com.linkedin.python.importer.ivy.IvyRepo
import com.linkedin.python.importer.pypi.cache.ApiCache
import com.linkedin.python.importer.pypi.client.Client
import groovy.util.logging.Slf4j

@Slf4j
class DownloaderFactory {
    private final IvyRepo localIvyRepo
    private final DependencySubstitution replacements
    private final ApiCache cache
    private final Client pypiClient
    private final PackageFactory packageFactory
    private final boolean lenient

    DownloaderFactory(IvyRepo localIvyRepo, DependencySubstitution replacements, ApiCache cache, Client pypiClient,
                      PackageFactory packageFactory, boolean lenient) {
        this.localIvyRepo = localIvyRepo
        this.replacements = replacements
        this.cache = cache
        this.pypiClient = pypiClient
        this.packageFactory = packageFactory
        this.lenient = lenient
    }

    Downloader createDownloader(String dependency) {
        Downloader downloader
        def parts = dependency.split(":")
        if (parts.length == 2) { // <module> : <version>
            downloader = new SdistDownloader(dependency, localIvyRepo, cache, pypiClient, packageFactory)
        } else if (parts.length == 3) { // <module> : <version> : <classifier>
            downloader = new WheelsDownloader(dependency, localIvyRepo, cache, pypiClient, packageFactory)
        } else {
            String errMsg = "Unable to parse the dependency "
            +dependency
            +".\nThe format of dependency should be either <module>:<revision> for source distribution "
            +"or <module>:<revision>:<classifier> for Wheels."

            if (lenient) {
                log.error(errMsg)
                return null
            }
            throw new IllegalArgumentException(errMsg)
        }

        if (lenient) {
            return new LenientDownloaderDecorator(downloader)
        }
        return downloader
    }
}
