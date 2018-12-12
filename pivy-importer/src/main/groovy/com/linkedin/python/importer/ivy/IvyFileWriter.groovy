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
package com.linkedin.python.importer.ivy

import com.linkedin.python.importer.deps.Dependency
import com.linkedin.python.importer.deps.DependencyType
import com.linkedin.python.importer.pypi.VersionEntry
import groovy.xml.MarkupBuilder
import org.apache.commons.io.FilenameUtils

class IvyFileWriter {
    final Dependency dependency
    final List<VersionEntry> archives

    IvyFileWriter(Dependency dependency, List<VersionEntry> archives) {
        this.dependency = dependency
        this.archives = archives
    }

    @SuppressWarnings("SpaceAroundClosureArrow")
    def writeIvyFile(File destDir, Map<String, List<String>> dependenciesMap) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.setDoubleQuotes(true)

        def pub = getPublicationsMap()

        if (!(pub.any { it.conf == 'default' })) {
            pub.first().conf = 'default'
        }

        xml.'ivy-module'(version: "2.0", 'xmlns:e': "http://ant.apache.org/ivy/extra", 'xmlns:m': "http://ant.apache.org/ivy/maven") {
            // fix the issue MarkupBuilder escapes ">" into "&gt"
            setEscapeAttributes(false)
            info(organisation:dependency.type.org, module: dependency.moduleName, revision: dependency.version)
            configurations {
                def configurations = new HashSet<>(dependenciesMap.keySet())
                configurations.add("source")
                configurations.each {
                    def map = [name: it, description: 'auto generated configuration for ' + it]
                    if ('default' != it) {
                        map['extends'] = 'default'
                    }
                    conf(map)
                }
            }
            publications {
                pub.each { archive ->
                    artifact(archive)
                }
            }
            dependencies(defaultconfmapping: "*->default") {
                dependenciesMap.each { config, deps ->
                    deps.each { dep ->
                        def (name, version) = dep.split(':')
                        dependency(org: 'pypi', name: name, rev: version, conf: config)
                    }
                }
            }
        }

        def ivyText = writer.toString()

        if (dependency.type == DependencyType.SOURCE_DISTRIBUTION) {
            new File(destDir, "${dependency.moduleName}-${dependency.version}.ivy").text = ivyText
        } else if (dependency.type == DependencyType.WHEEL) {
            new File(destDir, "${dependency.moduleName}-${dependency.version}-${dependency.classifier}.ivy").text = ivyText
        }
    }

    private getPublicationsMap() {
        def publicationMap = archives.collect { artifact ->
            def ext = artifact.filename.contains(".tar.") ? artifact.filename.find('tar\\..*') : FilenameUtils.getExtension(artifact.filename)
            String filename = artifact.filename - ("." + ext)
            def source = DependencyType.SOURCE_DISTRIBUTION == artifact.packageType
            def map = [name: dependency.moduleName, ext: ext, conf: source ? 'source' : 'default', type: ext]

            if (filename.indexOf(dependency.version) + dependency.version.length() + 1 < filename.length()) {
                map['m:classifier'] = getClassifier(filename)
            }
            return map
        }

        return publicationMap
    }

    private String getClassifier(String filename) {
        return filename.substring(filename.indexOf(dependency.version) + version.length() + 1)
    }
}
