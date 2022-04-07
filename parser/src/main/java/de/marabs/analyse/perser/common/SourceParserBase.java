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
package de.marabs.analyse.perser.common;

import de.marabs.analyse.perser.common.library.Library;
import de.marabs.analyse.common.constant.ParserConstants;
import lombok.Synchronized;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static de.marabs.analyse.common.constant.CommonConstants.*;
import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static java.util.Objects.*;
import static org.antlr.v4.runtime.tree.ParseTreeWalker.DEFAULT;

/**
 * {@code SourceParserBase} is the basis of all source code parsers.
 *
 * @author Martin Absmeier
 */
public abstract class SourceParserBase {

    private static final Logger LOGGER = LogManager.getLogger(SourceParserBase.class);

    protected final ParseTreeWalker treeWalker;
    protected final ApplicationBase application;
    protected final StopWatch sw;
    protected final StopWatch listenerSw;
    protected Integer numberOfFiles;
    protected Integer countFiles;
    protected List<ListenerBase> defaultListeners;
    protected List<ListenerBase> listeners;

    /**
     * Creates a new instance and initializes it.
     *
     * @param application the underlying application (e.g. JavaApplication).
     */
    public SourceParserBase(ApplicationBase application) {
        this.application = application;
        this.treeWalker = DEFAULT;
        this.sw = new StopWatch();
        this.listenerSw = new StopWatch();
        this.defaultListeners = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    // #################################################################################################################

    /**
     * Parses the files specified by {@code filesToParse}.
     *
     * @param files         the files to be parsed
     */
    public abstract void parseFiles(List<File> files);

    /**
     * Set the specified {@code listeners}.
     */
    public void setListeners(List<ListenerBase> listeners) {
        requireNonNull(listeners, "NULL is not permitted as a value for the 'listeners' parameter.");
        this.listeners = listeners;
    }

    /**
     * Add the specified {@code listener} if the list does not contain the listener already.
     *
     * @param listener the listener to be added
     */
    public void addListener(ListenerBase listener) {
        requireNonNull(listener, "NULL is not permitted as a value for the 'listener' parameter.");
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes all the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Executes the parse with the specified prediction mode {@code mode} on the specified {@code file}.
     *
     * @param file the file ti
     * @param mode the prediction mode to be used
     * @return the result of the parser
     * @throws IOException if an error occurs
     */
    protected abstract ParserResult tryPredictionMode(File file, PredictionMode mode) throws IOException;

    /**
     * Initializes the application with the library specified by {@code entry}.
     *
     * @param entry the entry of the library
     */
    protected abstract void initLibrary(Library entry);

    protected abstract void initDefaultListeners();

    // #################################################################################################################

    /**
     * Parses the source code and returns the {@link ParserResult}.
     *
     * @param file the source code to be parsed
     * @return the parser result or NULL if the file can not be parsed
     */
    @Synchronized
    protected ParserResult parseFile(File file) {
        try {
            try {
                return tryPredictionMode(file, PredictionMode.LL);
            } catch (IOException | RecognitionException | ParseCancellationException ex) {
                return tryPredictionMode(file, PredictionMode.LL_EXACT_AMBIG_DETECTION);
            }
        } catch (IOException ex) {
            String fileName = cleanupFileName(file.getAbsolutePath());
            LOGGER.error("Can not parse file [{}] due to: {}", fileName, ex);
        } catch (RecognitionException | ParseCancellationException ex) {
            String fileName = cleanupFileName(file.getAbsolutePath());
            LOGGER.error("Parsing error in file [{}] due to: {}", fileName, ex);
        }
        return null;
    }

    /**
     * Executes the parser for all specified {@code files}.
     *
     * @param files the files to be parsed
     * @return the parsing results
     */
    @Synchronized
    protected List<ParserResult> executeParser(List<File> files) {
        sw.start();

        countFiles = 1;
        numberOfFiles = files.size();

        List<ParserResult> parserResults = new ArrayList<>(numberOfFiles);
        files.stream()
            .map(this::parseFile)
            .filter(Objects::nonNull)
            .forEach(parserResult -> {
                countFiles++;
                parserResults.add(parserResult);
            });

        sw.stop();
        LOGGER.info(SEPARATOR);
        LOGGER.info("{} processed {} files in {}.", this.getClass().getSimpleName(), numberOfFiles, sw);
        LOGGER.info(SEPARATOR);
        sw.reset();

        return parserResults;
    }

    /**
     * Execute the specified {@code listener} on all specified {@code parserResults}.
     *
     * @param parserResults the parser results
     * @param listener      the listener to be executed
     */
    @Synchronized
    protected void executeListener(List<ParserResult> parserResults, ListenerBase listener) {
        sw.start();
        String listenerName = listener.getClass().getSimpleName();

        countFiles = 1;
        numberOfFiles = parserResults.size();

        parserResults.forEach(parserResult -> {
            listenerSw.start();
            listener.setSourceName(parserResult.getSourceName());
            treeWalker.walk(listener, parserResult.getParseTree());
            application.mergeWithApplication(listener.getResult());
            listener.reset();
            listenerSw.stop();

            LOGGER.info("Executed [{}] | Duration [{}] | File [{} of {}] -> {}", listenerName, listenerSw.toString(), countFiles, numberOfFiles, parserResult.getSourceName());
            countFiles++;
            listenerSw.reset();
        });

        sw.stop();
        LOGGER.info(SEPARATOR);
        LOGGER.info("{} processed {} files in {}.", listenerName, numberOfFiles, sw);
        LOGGER.info(SEPARATOR);
        sw.reset();
    }

    /**
     * Replaces the {@link ParserConstants#USER_DIR} and {@link ParserConstants#USER_HOME_DIR} with empty string.
     *
     * @param fileName the file name to be cleaned up
     * @return the cleaned up file name
     */
    @Synchronized
    protected String cleanupFileName(String fileName) {
        if (fileName.contains(USER_DIR)) {
            fileName = fileName.replace(USER_DIR, EMPTY_STRING);
        }
        if (fileName.contains(USER_HOME_DIR)) {
            fileName = fileName.replace(USER_HOME_DIR, EMPTY_STRING);
        }

        String resPath = format("{0}src{1}main{2}resources{3}", separator, separator, separator, separator);
        if (fileName.contains(resPath)) {
            fileName = fileName.replace(resPath, "");
        }
        resPath = format("{0}src{1}test{2}resources{3}", separator, separator, separator, separator);
        if (fileName.contains(resPath)) {
            fileName = fileName.replace(resPath, "");
        }

        return fileName;
    }

    /**
     * Initializes the the underlying application (e.g. JavaApplication) with the specified {@code  libraries}.
     *
     * @param libraries the libraries
     */
    @Synchronized
    protected void initLibraries(Library... libraries) {
        if (nonNull(libraries)) {
            for (Library library : libraries) {
                sw.start();
                initLibrary(library);
                sw.stop();
                LOGGER.info(format("Library: {0}:{1}:{2} - {3} loaded in [{4}]",
                                   library.getGroupId(), library.getArtifactId(), library.getVersion(), library.getRevisionId(), sw.toString()));
                sw.reset();
            }
        }
    }

    /**
     * Determines the id of the revision to be parsed.
     *
     * @return the revision id
     */
    protected String determineRevisionId() {
        // FIXME Determine revisionId -> implement it
        return UUID.randomUUID().toString();
    }

    /**
     * Returns the listeners of this parser.
     *
     * @return the listeners
     */
    protected List<ListenerBase> getListeners() {
        return listeners;
    }
}