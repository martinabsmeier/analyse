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

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit test cases of {@link FileUtils} class.
 *
 * @author Martin Absmeier
 */
public class FileUtilsTest {

    @Test(expected = NullPointerException.class)
    public void findFilesDirectoryNull() {
        FileUtils.findFiles(null, "java");
    }

    @Test(expected = NullPointerException.class)
    public void findFilesExtensionNull() {
        FileUtils.findFiles(new File(""), null);
    }

    @Test
    public void findFiles() {
        String path = System.getProperty("user.dir");
        List<File> actual = FileUtils.findFiles(new File(path), "java");

        assertNotNull("We expect a new created temp directory !", actual);
        assertFalse("We expect that there are files !", actual.isEmpty());
    }

    @Test
    public void createTempDirectory() {
        Path actual = FileUtils.createTempDirectory("directoryPrefix");
        assertNotNull("We expect a new created temp directory !", actual);
    }

    @Test
    public void createTempFile() {
        File actual = FileUtils.createTempFile("filePrefix", "fileSuffix");
        assertNotNull("We expect a new created temp directory !", actual);
    }
}