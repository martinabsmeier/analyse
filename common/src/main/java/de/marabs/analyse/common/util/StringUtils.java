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

import de.marabs.analyse.common.constant.CommonConstants;
import lombok.Synchronized;

import java.nio.charset.Charset;

import static de.marabs.analyse.common.constant.CommonConstants.EMPTY_STRING;
import static de.marabs.analyse.common.constant.CommonConstants.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code StringUtils} is a convenience class that simplifies the handling of strings.
 *
 * @author Martin Absmeier
 */
public class StringUtils {

    /**
     * Encode the string specified by {@code value} with {@link CommonConstants#UTF_8} encoding.
     *
     * @param value   the string to be encoded
     * @return the encoded string or "" if encode was null or empty.
     */
    @Synchronized
    public static String encode(String value) {
        return isNullOrEmpty(value) ? EMPTY_STRING : new String(value.getBytes(), UTF_8);
    }

    /**
     * Encode the string specified by {@code value} with @code charset} encoding.
     *
     * @param value   the string to be encoded
     * @param charset the charset used for encoding
     * @return the encoded string or "" if encode was null or empty.
     */
    @Synchronized
    public static String encode(String value, Charset charset) {
        requireNonNull(charset, "NULL is not permitted as value for 'charset' parameter.");
        return isNullOrEmpty(value) ? EMPTY_STRING : new String(value.getBytes(), charset);
    }

    /**
     * Encode the string specified by {@code value} with {@code charsetName} {@link Charset}.<br>
     *
     * @param value       the string to be encoded
     * @param charsetName the name of the charset
     * @return the encoded string or "" if encode was null or empty.
     */
    @Synchronized
    public static String encode(String value, String charsetName) {
        requireNonNull(charsetName, "NULL is not permitted as value for 'charsetName' parameter.");
        return isNullOrEmpty(value) ? EMPTY_STRING : new String(value.getBytes(), Charset.forName(charsetName));
    }

    /**
     * Limit the string specified by {@code value} to {@code length}.
     *
     * @param value  the string to be limited
     * @param length the length
     * @return the limited string
     * @throws IllegalArgumentException if length <= 0
     */
    @Synchronized
    public static String limit(String value, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("The parameter length must be > 0.");
        }
        if (isNullOrEmpty(value)) {
            return EMPTY_STRING;
        }
        return (value.length() >= length) ? value.substring(0, length - 1) : value;
    }

    /**
     * Checks if the the string specofoed by {@code value} is NULL or empty.
     *
     * @param value the string to be checked
     * @return true if value is NULL od empty false otherwise
     */
    @Synchronized
    public static boolean isNullOrEmpty(String value) {
        return isNull(value) || value.isEmpty();
    }

    // #################################################################################################################

    private StringUtils() {
        // We do not want an instance
    }
}