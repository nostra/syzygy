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

package no.api.syzygy.programmatic;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyDynamicLoader;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class DummyImplementation implements SyzygyDynamicLoader {

    @Override
    public SyzygyConfig createSyzygyConfigWith(final String configurationString, SyzygyConfig loaderConfiguration) {
        return new SyzygyConfig() {

            @Override
            public String getName() {
                return configurationString;
            }

            @Override
            public String lookup(String key) {
                return lookup(key, String.class);
            }

            @Override
            public <T> T lookup(String key, Class<T> clazz) {
                return (T) key;
            }

            @Override
            public Set<String> keys() {
                return new HashSet<>();
            }
        };
    }
}
