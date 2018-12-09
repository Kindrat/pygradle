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
class LenientDownloaderDecorator implements Downloader {
    private final Downloader delegate

    LenientDownloaderDecorator(Downloader delegate) {
        this.delegate = delegate
    }

    @Override
    void download(boolean latestVersions, boolean allowPreReleases, boolean fetchExtras) {
        delegate.download(latestVersions, allowPreReleases, fetchExtras)
    }

    @Override
    void downloadDependency(String dep, boolean latestVersions, boolean allowPreReleases, boolean fetchExtras) {
        try {
            delegate.downloadDependency(dep, latestVersions, allowPreReleases, fetchExtras)
        } catch (Exception e) {
            log.error("Unable to load $dep. ${e.message}")
        }
    }
}
