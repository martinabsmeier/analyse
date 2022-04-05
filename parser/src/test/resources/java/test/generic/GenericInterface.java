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
package test.generic;

import java.io.Serializable;

public interface GenericInterface {

    default void defaultMethod(Integer erg0) {
        class InterfaceMethodClass<Q extends Object & Serializable> {
            Q somethingElse = null;

            public Integer myMethod() {
                return 0;
            }
        }
    }

    Class<? extends String> bar(Integer arg1);

    Class<? extends Integer> fooBar(Class<? extends Integer>... numbers);

    void foo(Class<?>... arg0);

    String[] getAll();

    void setMore(Integer[] array1, String[] array2);

    void setPrimitive(int[] intArray, boolean argBool);

    default void defaultMethod() { }
}