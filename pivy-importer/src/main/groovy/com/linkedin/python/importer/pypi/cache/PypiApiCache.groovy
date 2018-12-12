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
package com.linkedin.python.importer.pypi.cache

import com.linkedin.python.importer.deps.Dependency
import com.linkedin.python.importer.pypi.details.ProjectDetails
import com.linkedin.python.importer.pypi.details.ProjectDetailsAware
import com.linkedin.python.importer.pypi.client.Client
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpResponseException

@Slf4j
class PypiApiCache implements ApiCache {
    final Client pypiClient
    Map<String, ProjectDetails> cache = [:].withDefault { String dependency ->
        pypiClient.downloadMetadata(dependency)
    }

    PypiApiCache(Client pypiClient) {
        this.pypiClient = pypiClient
    }

    ProjectDetailsAware getDetails(String dependency) {
        try {
            return cache.get(dependency)
        } catch (HttpResponseException httpResponseException) {
            String msg = "Package ${dependency} has an illegal module name, " +
                "we are not able to find it on PyPI (https://pypi.org/pypi/$dependency/json)"
            throw new IllegalArgumentException("$msg. ${httpResponseException.message}")
        }
    }

    @Override
    ProjectDetailsAware getDetails(Dependency dependency) {
        try {
            return cache.get(dependency.moduleName)
        } catch (HttpResponseException httpResponseException) {
            String msg = "Package ${dependency} has an illegal module name, " +
                "we are not able to find it on PyPI (https://pypi.org/pypi/$dependency.moduleName/json)"
            throw new IllegalArgumentException("$msg. ${httpResponseException.message}")
        }
    }
}
