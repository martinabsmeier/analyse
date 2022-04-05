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
package de.marabs.analyse.perser.java;

import de.marabs.analyse.perser.common.library.Library;
import de.marabs.analyse.parser.generated.java.JavaLexer;
import de.marabs.analyse.parser.generated.java.JavaParser;
import de.marabs.analyse.perser.common.LoggingErrorListener;
import de.marabs.analyse.perser.common.ParserResult;
import de.marabs.analyse.perser.common.SourceParserBase;
import de.marabs.analyse.perser.java.listener.JavaDeclarationListener;
import de.marabs.analyse.perser.java.listener.JavaStructureListener;
import lombok.Builder;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static de.marabs.analyse.common.constant.CommonConstants.SEPARATOR;
import static java.util.Objects.requireNonNull;

/**
 * {@code JavaSourceParser} parses java source code files and execute the listeners.
 *
 * @author Martin Absmeier
 */
public class JavaSourceParser extends SourceParserBase {

    private static final Logger LOGGER = LogManager.getLogger(JavaSourceParser.class);

    /**
     * Creates a new instance of {@code JavaSourceParser} with the specified {@code libraries} class.
     *
     * @param libraries the {@link Library} to be initialized before parsing
     */
    @Builder
    public JavaSourceParser(Library... libraries) {
        super(JavaApplication.getInstance());
        initDefaultListeners();
        initLibraries(libraries);
    }

    @Override
    public void parseFiles(List<File> files) {
        requireNonNull(files, "NULL is not permitted as a value for the 'files' parameter.");

        LOGGER.info("Start parsing [{}] files.", files.size());
        LOGGER.info(SEPARATOR);

        List<ParserResult> parserResults = executeParser(files);
        if (listeners.isEmpty()) {
            LOGGER.warn(SEPARATOR);
            LOGGER.warn("Executing DEFAULT listeners !");
            LOGGER.warn(SEPARATOR);
            defaultListeners.forEach(listener -> executeListener(parserResults, listener));
        } else {
            listeners.forEach(listener -> executeListener(parserResults, listener));
        }
    }

    @Override
    protected ParserResult tryPredictionMode(File file, PredictionMode mode) throws IOException {
        if (!parseSw.isStarted()) {
            parseSw.start();
        }

        String fileName = cleanupFileName(file.getAbsolutePath());
        JavaParser parser = buildParser(file, mode);
        ParserResult parserResult = ParserResult.builder()
            .parseTree(parser.compilationUnit())
            .sourceName(fileName)
            .build();

        parseSw.stop();
        LOGGER.info("Executed [{}] | Mode [{}] | Duration [{}] | File [{} of {}] -> {}",
                    this.getClass().getSimpleName(), mode, parseSw, countFiles, numberOfFiles, fileName);
        parseSw.reset();

        return parserResult;
    }

    @Override
    protected void initLibrary(Library entry) {
        requireNonNull(entry, "NULL is not permitted as a value for the 'entry' parameter.");

        /*
        LibAppManager manager = LibAppManager.builder()
            //.database(PersistenceFacadeDb.getInstance(provider))
            .build();
        List<Library> libraries = manager.findLibrary(entry);
        if (nonNull(libraries) && !libraries.isEmpty()) {
            libraries.forEach(library -> application.addLibrary(library.getLibrary()));
        }
        */
    }

    @Override
    protected void initDefaultListeners() {
        String revisionId = determineRevisionId();

        defaultListeners.addAll(List.of(
            new JavaDeclarationListener(revisionId),
            new JavaStructureListener(revisionId)
        ));
    }

    // #################################################################################################################
    // Private methods

    private JavaParser buildParser(File file, PredictionMode mode) throws IOException {
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(file.getAbsolutePath()));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new LoggingErrorListener());

        JavaParser parser = new JavaParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        parser.getInterpreter().setPredictionMode(mode);

        return parser;
    }
}