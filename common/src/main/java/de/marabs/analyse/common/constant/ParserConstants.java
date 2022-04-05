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

    public static final String UNIQUE_DELIMITER = CONFIG.get(String.class, "uniqueDelimiter");
    public static final String DELIMITER_REGEX = "[.]";

    // #################################################################################################################
    public static final String JAVA_LANG_PACKAGE = "java.lang";
    public static final String JAVA_IO_PACKAGE = "java.io";
    public static final String JAVA_DEFAULT_PACKAGE_NAME = "default";

    public static final String JAVA_MODIFIER_PUBLIC = "public";
    public static final String JAVA_MODIFIER_PROTECTED = "protected";
    public static final String JAVA_MODIFIER_STATIC = "static";
    public static final String JAVA_MODIFIER_FINAL = "final";

    public static final String JAVA_INSTANCE_INITIALIZER_NAME = "//instance_initializer";
    public static final String JAVA_STATIC_INITIALIZER_NAME = "//static_initializer";

    public static final String JAVA_ATT_KEY_METHOD_SIGNATURE = "method signature";
    public static final String JAVA_ATT_KEY_CONTAINING_CLASS = "containing class";

    // #################################################################################################################
    private ParserConstants() {
        // We do not want an instance
    }
}