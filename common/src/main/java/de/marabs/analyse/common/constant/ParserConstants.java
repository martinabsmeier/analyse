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
package de.marabs.analyse.common.constant;

/**
 * {@code ParserConstants} contains the common constants of module {@code parser}.
 *
 * @author Martin Absmeier
 */
public class ParserConstants extends CommonConstants {

    public static final String NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM = "NULL is not permitted as a value for the 'component' parameter.";
    public static final String NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM = "NULL is not permitted as a value for the 'uniqueCoordinate' parameter.";

    public static final String UNIQUE_DELIMITER = CONFIG.get(String.class, "uniqueDelimiter");
    public static final String DELIMITER_REGEX = "[.]";

    // #################################################################################################################
    private ParserConstants() {
        // We do not want an instance
    }
}