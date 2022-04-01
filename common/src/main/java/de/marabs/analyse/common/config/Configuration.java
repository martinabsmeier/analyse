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

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * {@code Configuration} is responsible for the configuration of the application.
 *
 * @author Martin Absmeier
 */
public class Configuration {
    private static final Logger LOGGER = LogManager.getLogger(Configuration.class);

    private static final String COMMON_DEFINITION = "configDefinition.xml";
    private static final String COMMON_DEFINITION_TEST = "testConfigDefinition.xml";

    @Getter
    private CombinedConfiguration configuration;

    @Builder
    public Configuration(boolean isTestConfiguration) {
        String configDefinition = isTestConfiguration ? COMMON_DEFINITION_TEST : COMMON_DEFINITION;

        try {
            initConfiguration(configDefinition);
            LOGGER.info("Configuration successfully loaded from '{}' file.", configDefinition);
        } catch (ConfigurationException ex) {
            LOGGER.error("Can not load configuration from '{}' file due to '{}'", configDefinition, ex.getMessage());
            LOGGER.error(ex);
        }
    }

    // #################################################################################################################
    private void initConfiguration(String configDefinition) throws ConfigurationException {
        Parameters params = new Parameters();
        CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(params.fileBased().setFile(new File(configDefinition)));
        configuration = builder.getConfiguration();
    }
}