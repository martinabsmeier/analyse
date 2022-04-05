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
package de.marabs.analyse.common.exception;

/**
 * {@code ParseException} is an unchecked exception isn't needed to be declared in a method or constructor's throws clause.
 *
 * @author Martin Absmeier
 */
public class ParseException extends RuntimeException {

    /**
     * Constructs a new {@code ParseException} with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to initCause.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the getMessage() method.
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ParseException} with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted,
     *                and indicates that the cause is nonexistent or unknown.)
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code ParseException} with the specified cause and a detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     * This constructor is useful for runtime exceptions that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted,
     *              and indicates that the cause is nonexistent or unknown.)
     */
    public ParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code ParseException} with the specified detail message, cause, suppression enabled or disabled,
     * and writable stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}