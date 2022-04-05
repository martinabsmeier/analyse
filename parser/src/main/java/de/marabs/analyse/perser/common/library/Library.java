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
package de.marabs.analyse.perser.common.library;

import de.marabs.analyse.common.component.Component;
import de.marabs.analyse.common.component.type.ComponentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code Library} represents an entry of a library.
 *
 * @author Martin Absmeier
 */
@Builder
public class Library {

    @Getter
    private final String groupId;
    @Getter
    private final String artifactId;
    @Getter
    private final String version;
    @Getter
    private String revisionId;
    @Getter @Setter
    private ComponentType type;
    @Getter @Setter
    private Component library;

}