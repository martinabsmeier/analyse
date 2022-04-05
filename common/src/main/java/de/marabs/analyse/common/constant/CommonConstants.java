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

import de.marabs.analyse.common.config.Configuration;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.io.File.separator;

/**
 * {@code CommonConstants} contains general constants and is the superclass of all classes containing constants.<br>
 * <b>NOTE:</b><br>
 * Other constant classes must be derived from this one.
 *
 * @author Martin Absmeier
 */
public class CommonConstants {
    protected static final CombinedConfiguration CONFIG = Configuration.builder().isTestConfiguration(false).build().getConfig();

    public static final String EMPTY_STRING = "";
    public static final String SEPARATOR = CONFIG.get(String.class, "separator");
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String USER_HOME_DIR = System.getProperty("user.home");
    public static final String TARGET_DIR = USER_DIR + separator + "target" + separator;

    // #################################################################################################################

    public static final String NULL_NOT_PERMITTED_AS_VALUE_TYPE = "NULL is not permitted as value for parameter 'type'.";
    public static final String NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM = "NULL is not permitted as a value for the 'component' parameter.";
    public static final String NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM = "NULL is not permitted as a value for the 'uniqueCoordinate' parameter.";

    // #################################################################################################################
    protected CommonConstants() {
        // We do not want an instance
    }
}