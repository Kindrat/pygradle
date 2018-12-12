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
package com.linkedin.python.importer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.linkedin.python.importer.deps.DependencySubstitution;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImporterCLI {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImporterCLI.class);

    private ImporterCLI() {
        //private constructor for "util" class
    }

    public static void main(String[] args) throws Exception {
        Options options = createOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("quiet")) {
            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.WARN);
        }

        if (line.hasOption("debug")) {
            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.DEBUG);
        }

        if (!line.hasOption("repo")) {
            throw new RuntimeException("Unable to continue, no repository location given on the command line (use the"
                + " --repo switch)");
        }
        final File repoPath = new File(line.getOptionValue("repo"));

        repoPath.mkdirs();

        if (!repoPath.exists() || !repoPath.isDirectory()) {
            throw new RuntimeException("Unable to continue, " + repoPath.getAbsolutePath() + " does not exist, or is "
                + "not a directory");
        }

        importPackages(line, repoPath);
        logger.info("Execution Finished!");
    }

    private static void importPackages(CommandLine line, File repoPath) {
        DependencySubstitution replacements = new DependencySubstitution(buildSubstitutionMap(line),
            buildForceMap(line));

        boolean lenient = line.hasOption("lenient");
        boolean allowPreReleases = line.hasOption("pre");
        boolean fetchExtras = line.hasOption("extras");
        boolean latestVersions = line.hasOption("latest");
        String localRepo = line.getOptionValue("local_repo");

        new PypiImporter(lenient, allowPreReleases, fetchExtras, latestVersions, repoPath, localRepo)
            .importDependencies(replacements, line.getArgs());
    }

    private static Map<String, String> buildForceMap(CommandLine line) {
        Map<String, String> sub = new LinkedHashMap<>();
        if (line.hasOption("force")) {
            for (String it : line.getOptionValues("force")) {
                String[] split = it.split(":");
                sub.put(split[0], split[1]);
            }
        }

        return sub;
    }

    private static Options createOptions() {
        Option repo = Option.builder()
            .longOpt("repo")
            .numberOfArgs(1)
            .argName("file")
            .desc("location of the ivy repo")
            .build();

        Option replacement = Option.builder()
            .longOpt("replace")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .argName("replacement")
            .desc("Noted like orig:version=replace:version")
            .valueSeparator(',')
            .build();

        Option quiet = Option.builder()
            .longOpt("quiet")
            .numberOfArgs(0)
            .desc("Sets logging level to WARN")
            .build();

        Option debug = Option.builder()
            .longOpt("debug")
            .numberOfArgs(0)
            .desc("Sets logging level to DEBUG")
            .build();

        Option force = Option.builder()
            .longOpt("force")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .desc("Noted like name:version")
            .valueSeparator(',')
            .build();

        Option latest = Option.builder()
            .longOpt("latest")
            .numberOfArgs(0)
            .desc("Gets latest versions of dependencies")
            .build();

        Option pre = Option.builder()
            .longOpt("pre")
            .numberOfArgs(0)
            .desc("Allows pre-releases (alpha, beta, release candidates)")
            .build();

        Option extras = Option.builder()
            .longOpt("extras")
            .numberOfArgs(0)
            .desc("Gets extra dependencies for each dependency")
            .build();

        Option lenient = Option.builder()
            .longOpt("lenient")
            .numberOfArgs(0)
            .desc("Allows to import all available dependencies with logging missed")
            .build();

        Option localRepo = Option.builder()
            .longOpt("local_repo")
            .numberOfArgs(1)
            .desc("Local repo path (file or dir)")
            .build();

        Options options = new Options();
        options.addOption(replacement);
        options.addOption(repo);
        options.addOption(quiet);
        options.addOption(debug);
        options.addOption(force);
        options.addOption(latest);
        options.addOption(pre);
        options.addOption(extras);
        options.addOption(lenient);
        options.addOption(localRepo);

        return options;
    }

    private static Map<String, String> buildSubstitutionMap(CommandLine line) {
        Map<String, String> sub = new LinkedHashMap<>();
        if (line.hasOption("replace")) {
            for (String it : line.getOptionValues("replace")) {
                String[] split = it.split("=");
                sub.put(split[0], split[1]);
            }
        }
        return sub;
    }
}
