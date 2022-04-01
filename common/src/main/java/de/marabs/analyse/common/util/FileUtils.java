/*
 * Copyright 2022 Martin Absmeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.marabs.analyse.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Martin Absmeier
 */
public class FileUtils {
    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    /**
     * Retrieve all files from the specified {@code directory} matching the {@code extension}.
     *
     * @param directory  the directory to be parsed
     * @param extensions the extension of the file (e.g. 'java')
     * @return list with the found files
     * @throws IllegalArgumentException is thrown if parameter {@code directory} is not a directory
     */
    public static List<File> findFiles(File directory, List<String> extensions) throws IllegalArgumentException {
        requireNonNull(directory, "NULL is not permitted as value for parameter 'directory'.");
        requireNonNull(extensions, "NULL is not permitted as value for parameter 'extensions'.");

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Please specify a directory. '" + directory.getName() + "' is not a directory.");
        }

        return extensions.stream()
            .map(ext -> findFiles(directory, ext))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Creates a new directory in the default temporary-file directory, using the given prefix to generate its name.
     * The resulting Path is associated with the default FileSystem.
     *
     * @param prefix the prefix string to be used in generating the directory's name; may be null
     * @return the path to the newly created directory that did not exist before this method was invoked
     * or NULL if an error occurs
     */
    public static Path createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException ex) {
            LOGGER.error("Can not create temp directory due to: {}", ex.getMessage());
            LOGGER.error(ex);
            return null;
        }
    }

    /**
     * Creates an empty file in the default temporary-file directory, using the given prefix and suffix to generate its name.
     *
     * @param prefix the prefix string to be used in generating the file's name; must be at least three characters long
     * @param suffix the suffix string to be used in generating the file's name; may be null, in which case the suffix ".tmp" will be used
     * @return a newly-created empty file or NULL if an error occurs
     */
    public static File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix);
        } catch (IOException ex) {
            LOGGER.error("Can not create temp file due to: {}", ex.getMessage());
            LOGGER.error(ex);
            return null;
        }
    }

    // #################################################################################################################
    private static List<File> findFiles(File directory, String extension) {
        try (Stream<Path> paths = Files.walk(Paths.get(directory.getPath()))) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(extension))
                .map(Path::toFile)
                .collect(Collectors.toList());
        } catch (IOException ex) {
            LOGGER.warn("Could not read file from '{}' with extension '{}' due to: '{}'", directory, extension, ex.getMessage());
            return Collections.emptyList();
        }
    }
}