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
 * Dynamically loading programmatic configuration set. This class will be
 * instantiated and is supposed to create a SyzygyConfig configuration.
 * Note that the class needs a public argument free constructor.
 */
public interface SyzygyDynamicLoader {

    /**
     * @param configurationString A configuration string which can be used to get more information from the top
     *                            level configuration. At the very least, it can give you some way of differentiating
     *                            between two otherwise equal configuration sets.
     * @param loaderConfiguration The framework will send this in. It would be the
     *                            configuration of the top level configuration, i.e. <tt>syzygy.yaml</tt>
     * @return Configuration as determined from parameters. Null if global parameter
     * <tt>stop_if_error</tt> is false, and an error has occurred.
     */
    SyzygyConfig createSyzygyConfigWith( String configurationString, SyzygyConfig loaderConfiguration );
}
