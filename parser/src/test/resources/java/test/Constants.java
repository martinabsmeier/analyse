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
 * Checks if we process constants correctly.
 *
 * @author Martin Absmeier
 */
public interface Constants {
    int ONE = 1;
    int TWO = 2;
    int THREE = 3;

    interface MoreConstants extends Constants {
        int FOUR = 4;
        int FIVE = 5;
        int SIX = 6;
    }
}