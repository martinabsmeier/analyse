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
package de.marabs.analyse.perser;

import de.marabs.analyse.common.exception.ParseException;
import de.marabs.analyse.common.util.FileUtils;
import de.marabs.analyse.perser.common.ListenerBase;
import de.marabs.analyse.perser.common.SourceParserBase;
import de.marabs.analyse.perser.common.library.Library;
import de.marabs.analyse.perser.java.JavaSourceParser;
import lombok.Builder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marabs.analyse.common.constant.CommonConstants.SEPARATOR;
import static de.marabs.analyse.perser.SourceType.*;
import static java.util.Objects.*;

/**
 * {@code SourceParser} is responsible for executing the corresponding parser based on the supplied {@link SourceType}.
 *
 * @author Martin Absmeier
 */
public class SourceParser {

    private static final Logger LOGGER = LogManager.getLogger(SourceParser.class);

    private final List<ListenerBase> listeners;
    private final Map<SourceType, Library[]> libraries;

    /**
     * Creates a new instance of {@code SourcParser}.
     *
     * @param libraries the libraries for each source type
     */
    @Builder
    public SourceParser(List<ListenerBase> listeners, Map<SourceType, Library[]> libraries) {
        this.listeners = isNull(listeners) ? new ArrayList<>() : listeners;
        this.libraries = isNull(libraries) ? new HashMap<>() : libraries;
    }

    /**
     * Parses the files in the specified {@code directory}.
     *
     * @param type      the type of the source code
     * @param directory the directory to be parsed
     */
    public void parseDirectory(SourceType type, File directory) {
        requireNonNull(directory, "NULL is not permitted as a value for the 'directory' parameter.");

        List<String> extensions = findExtensionsByType(type);
        extensions.forEach(extension -> {
            List<File> filesToParse = FileUtils.findFiles(directory, extension);
            if (filesToParse.size() > 0) {
                LOGGER.info("Reading files with extension [{}}] from directory -> {}", extension ,directory.getAbsolutePath());
                LOGGER.info(SEPARATOR);

                SourceParserBase parser = findParserByType(type);
                parser.parseFiles(filesToParse);
            } else {
                LOGGER.info("No files found for extension [{}}] in directory -> {}", extension, directory.getAbsolutePath());
                LOGGER.info(SEPARATOR);
            }
        });
    }

    // #################################################################################################################
    private SourceParserBase findParserByType(SourceType type) {
        Library[] library = libraries.get(JAVA);

        SourceParserBase parser = null;
        switch (type) {
            case JAVA:
                if (nonNull(library)) {
                    parser = JavaSourceParser.builder().libraries(library).build();
                } else {
                    parser = JavaSourceParser.builder().build();
                }
                break;

            case SCALA:
                /*
                if (nonNull(library)) {
                    parser = CobolSourceParser.builder().provider(provider).libraries(library).params(createCobolParserParams()).build();
                } else {
                    parser = CobolSourceParser.builder().provider(provider).params(createCobolParserParams()).build();
                }
                */
                break;


            default:
                throw new ParseException("Can not find source parser for type: " + type.name());
        }

        if (nonNull(parser) && !listeners.isEmpty()) {
            parser.setListeners(listeners);
        }

        return parser;
    }

    private List<String> findExtensionsByType(SourceType type) {
        switch (type) {
            case JAVA:
                return List.of("java");
            case SCALA:
                return List.of("scala");
            default:
                throw new ParseException("Can not find extension for type: " + type.name());
        }
    }
}