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
package de.marabs.analyse.parser;

import de.marabs.analyse.perser.SourceParser;
import de.marabs.analyse.perser.SourceType;
import de.marabs.analyse.perser.common.ListenerBase;
import de.marabs.analyse.perser.common.library.Library;
import de.marabs.analyse.perser.java.JavaApplication;
import de.marabs.analyse.perser.java.listener.JavaDeclarationListener;
import de.marabs.analyse.perser.java.listener.JavaStructureListener;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static de.marabs.analyse.common.constant.CommonConstants.USER_DIR;
import static de.marabs.analyse.perser.SourceType.JAVA;
import static java.io.File.separator;
import static org.junit.Assert.assertNotNull;

/**
 * JUnit test cases of {@link SourceParser} class.
 *
 * @author Martin Absmeier
 */
public class SourceParserTest {

    private final String rootPath = USER_DIR.concat(separator)
        .concat("src").concat(separator)
        .concat("test").concat(separator)
        .concat("resources").concat(separator)
        .concat("java").concat(separator);
    private File directory;
    private final String revisionId = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        directory = new File(rootPath);
    }

    @Test
    public void testJavaSourceParser() {
        SourceParser parser = SourceParser.builder()
            .libraries(getLibraries())
            .listeners(getDefaultListeners(JAVA))
            .build();
        parser.parseDirectory(JAVA, directory);

        JavaApplication application = JavaApplication.getInstance();
        assertNotNull("We expect an instance.", application);
    }

    // #################################################################################################################
    private Map<SourceType, Library[]> getLibraries() {
        return new HashMap<>();

        /*
        LibAppManager libraryManager = LibAppManager.builder().build();
        Library library = libraryManager.findLibrary(GROUP_ID, ARTIFACT_ID, VERSION, REVISION_ID);
        if (Objects.nonNull(library)) {
            libraries.put(JAVA, new Library[]{library});
        }
        */
    }

    private List<ListenerBase> getDefaultListeners(SourceType type) {
        List<ListenerBase> defaultListeners = new ArrayList<>();

        if (type == JAVA) {
            defaultListeners.addAll(
                List.of(new JavaDeclarationListener(revisionId),
                        new JavaStructureListener(revisionId))
            );
        }

        return defaultListeners;
    }
}