package com.linkedin.python.importer;

import com.linkedin.python.importer.deps.Downloader;
import com.linkedin.python.importer.deps.DownloaderFactory;
import com.linkedin.python.importer.deps.DependencySubstitution;
import com.linkedin.python.importer.distribution.PackageFactory;
import com.linkedin.python.importer.ivy.IvyRepo;
import com.linkedin.python.importer.pypi.cache.ApiCache;
import com.linkedin.python.importer.pypi.cache.CacheFactory;
import com.linkedin.python.importer.pypi.client.Client;
import com.linkedin.python.importer.pypi.client.ClientFactory;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PypiImporter {
    private final Set<String> processedDependencies = new HashSet<>();
    private final boolean lenient;
    private final boolean allowPreReleases;
    private final boolean fetchExtras;
    private final boolean latestVersions;
    private final File repoPath;
    private final String localRepo;

    public PypiImporter(boolean lenient, boolean allowPreReleases, boolean fetchExtras, boolean latestVersions,
                        File repoPath, String localRepo) {
        this.lenient = lenient;
        this.allowPreReleases = allowPreReleases;
        this.fetchExtras = fetchExtras;
        this.latestVersions = latestVersions;
        this.repoPath = repoPath;
        this.localRepo = localRepo;
    }

    public void importDependencies(DependencySubstitution replacements, String... dependencies) {
        Client pypiClient = ClientFactory.create(localRepo);
        ApiCache cache = CacheFactory.create(pypiClient, allowPreReleases);
        PackageFactory packageFactory = new PackageFactory(cache, replacements);
        IvyRepo localIvyRepo = new IvyRepo(repoPath);

        DownloaderFactory downloaderFactory =
            new DownloaderFactory(localIvyRepo, replacements, cache, pypiClient, packageFactory, lenient);

        for (String dependency : dependencies) {
            processSingleDependency(downloaderFactory, dependency);
        }

    }

    private void processSingleDependency(DownloaderFactory downloaderFactory, String dependency) {
        if (processedDependencies.contains(dependency)) {
            return;
        }
        Downloader downloader = downloaderFactory.createDownloader(dependency);
        if (downloader != null) {
            List<String> nestedDependencies = downloader.download(latestVersions, allowPreReleases, fetchExtras);
            processedDependencies.add(dependency);
            nestedDependencies.forEach(dep -> processSingleDependency(downloaderFactory, dep));
        }
    }

}
