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
package de.marabs.analyse.perser.java.listener;

import de.marabs.analyse.perser.java.JavaParsingContext;

/**
 * {@code JavaDeclarationListener} is responsible for detecting top level type declarations.<br>
 * A top level type declaration is one of <i>class, interface or enum</i>.<br>
 * <b>Attention:</b><br>
 * This is an empty listener because all the work in done by {@link JavaListenerBase}, the only reason why this
 * listener exits is that abstract classes cannot be instantiated, and we need to process all files before the
 * next listener starts.
 *
 * @author Martin Absmeier
 */
public class JavaDeclarationListener extends JavaListenerBase {

    /**
     * Creates a new instance of {@code JavaDeclarationListener} class.
     *
     * @param revisionId revisionId the unique id of the source code
     */
    public JavaDeclarationListener(String revisionId) {
        super(JavaParsingContext.builder().revisionId(revisionId).build());
    }
}