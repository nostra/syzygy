/*
 * Copyright 2015 Amedia Utvikling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.api.syzygy;

/**
 * Provide an alternative of reading configuration data. It will get a configuration name, and
 * should return a SyzygyConfig if it is able to provide one, and null otherwise.
 */
public interface ConfigurationProvider {

    /**
     * @param topLevelConfig Top level configuration which may be used in order to obtain configuration detail
     * @param configurationName Name of the configuration which this provider might, eh, provide
     * @return null if none, or and instance of SyzygyConfig
     */
    SyzygyConfig findConfigurationFrom( SyzygyConfig topLevelConfig, String configurationName );
}
