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
