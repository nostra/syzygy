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
    public SyzygyConfig createSyzygyConfigWith(SyzygyConfig loaderConfiguration) {
        return new SyzygyConfig() {

            @Override
            public String getName() {
                return getClass().getName();
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
