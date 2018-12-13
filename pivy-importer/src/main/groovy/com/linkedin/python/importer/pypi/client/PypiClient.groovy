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
package com.linkedin.python.importer.pypi.client

import com.linkedin.python.importer.deps.DependencyType
import com.linkedin.python.importer.pypi.VersionEntry
import com.linkedin.python.importer.pypi.details.ProjectDetails
import com.linkedin.python.importer.pypi.details.ProjectDetailsAware
import com.linkedin.python.importer.util.ProxyDetector
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.fluent.Request

import static java.util.Collections.unmodifiableMap

@Slf4j
class PypiClient implements Client{

    @Override
    File downloadArtifact(String url) {
        def filename = FilenameUtils.getName(new URL(url).getPath())
        def contents = new File(FileUtils.getTempDirectory(), filename)

        if (!contents.exists()) {
            def proxy = ProxyDetector.maybeGetHttpProxy()

            def builder = Request.Get(url)
            if (null != proxy) {
                builder = builder.viaProxy(proxy)
            }

            for (int i = 0; i < 3; i++) {
                try {
                    builder.connectTimeout(5000)
                        .socketTimeout(5000)
                        .execute()
                        .saveContent(contents)
                    break
                } catch (SocketTimeoutException ignored) {
                    Thread.sleep(1000)
                }
            }
        }

        return contents
    }

    @Override
    ProjectDetailsAware downloadMetadata(String dependency) {
        def url = "https://pypi.org/pypi/$dependency/json"
        log.debug("Metadata url: {}", url)
        def proxy = ProxyDetector.maybeGetHttpProxy()

        def builder = Request.Get(url)
        if (null != proxy) {
            builder = builder.viaProxy(proxy)
        }
        def content = builder.connectTimeout(10000)
            .socketTimeout(10000)
            .execute().returnContent().asString()

        Map<String, Object> details = new JsonSlurper().parseText(content) as Map<String, Object>
        def name = details.info.name
        def latest = details.info.version
        Map<String, List<VersionEntry>> releases = new HashMap<>()

        details.releases.each { String version, entry ->
            releases[version] = entry.collect {
                it -> new VersionEntry(it.url, DependencyType.forPythonType(it.packagetype), it.filename, version) }
        }
        return new ProjectDetails(name, unmodifiableMap(releases), latest)
    }
}
