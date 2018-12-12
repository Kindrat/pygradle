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


import groovy.util.logging.Slf4j

@Slf4j
abstract class DependencyDownloader implements Downloader {
    final String project

    protected DependencyDownloader(String dependency) {
        this.project = dependency
    }

    @Override
    String dependency() {
        return project
    }
/**
     * Get the actual module name from artifact name, which has the correct letter case.
     * @param filename the filename of artifact
     * @param revision module version
     * @return actual module name, which is from PyPI
     */
    static String getActualModuleNameFromFilename(String filename, String revision) {
        return filename.substring(0, filename.indexOf(revision) - 1)
    }
}
