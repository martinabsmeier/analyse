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
package test;

/**
 * Check if we process fields correct.
 *
 * @author Martin Absmeier
 */
public class Fields {

    private static final String CONSTANST_1 = "CONSTANST_1";
    protected static final String CONSTANST_2 = "CONSTANST_2";
    public static final String CONSTANST_3 = "CONSTANST_3";
    public final int INT_CONSTANST = 1;

    private int field1;
    protected Double field2 = Double.valueOf(1);
    public Integer field3 = 2;
}