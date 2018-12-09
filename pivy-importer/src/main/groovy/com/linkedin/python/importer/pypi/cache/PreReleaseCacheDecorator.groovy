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
package com.linkedin.python.importer.pypi.cache

import com.linkedin.python.importer.pypi.PreReleaseProjectDetails
import com.linkedin.python.importer.pypi.ProjectDetailsAware

class PreReleaseCacheDecorator implements ApiCache {
    private final ApiCache delegate

    PreReleaseCacheDecorator(ApiCache delegate) {
        this.delegate = delegate
    }

    @Override
    ProjectDetailsAware getDetails(String project) {
        def details = delegate.getDetails(project)
        if (details == null) {
            return null
        }
        return new PreReleaseProjectDetails(details)
    }
}
