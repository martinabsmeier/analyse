/*
 + Copyright 2022 Martin Absmeier
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
package de.marabs.analyse.common.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * JUnit test cases of {@link Configuration} class.
 *
 * @author Martin Absmeier
 */
public class ConfigurationTest {

    @Test
    public void loadConfiguration() throws ConfigurationException {
        Configuration actual = Configuration.builder().build();
        assertNotNull("We expect an Configuration instance !", actual);

        CombinedConfiguration config = actual.getConfiguration();
        assertNotNull("We expect an configuration !", config);

        String separator = config.get(String.class, "separator");
        assertNotNull("We expect an separator !", separator);
    }

    @Test
    public void loadTestConfiguration() throws ConfigurationException {
        Configuration actual = Configuration.builder().isTestConfiguration(true).build();
        assertNotNull("We expect an Configuration instance !", actual);

        CombinedConfiguration config = actual.getConfiguration();
        assertNotNull("We expect an test configuration !", config);
    }
}