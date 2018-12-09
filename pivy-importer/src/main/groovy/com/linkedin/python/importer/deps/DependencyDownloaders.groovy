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

import com.linkedin.python.importer.pypi.cache.ApiCache
import groovy.util.logging.Slf4j

@Slf4j
class DependencyDownloaders {
    static Optional<Downloader> createDownloader(String dependency, File repoPath, DependencySubstitution replacements,
                                                 Set<String> processedDependencies, ApiCache cache, boolean lenient) {

        Downloader downloader
        def parts = dependency.split(":")
        if (parts.length == 2) { // <module> : <version>
            downloader = new SdistDownloader(dependency, repoPath, replacements, processedDependencies, cache)
        } else if (parts.length == 3) { // <module> : <version> : <classifier>
            downloader = new WheelsDownloader(dependency, repoPath, replacements, processedDependencies, cache)
        } else {
            String errMsg = "Unable to parse the dependency "
            +dependency
            +".\nThe format of dependency should be either <module>:<revision> for source distribution "
            +"or <module>:<revision>:<classifier> for Wheels."

            if (lenient) {
                log.error(errMsg)
                return Optional.empty()
            }
            throw new IllegalArgumentException(errMsg)
        }

        if (lenient) {
            return Optional.of(new LenientDownloaderDecorator(downloader))
        }
        return Optional.of(downloader)
    }
}
