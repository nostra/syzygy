package no.api.syzygy;

import no.api.syzygy.loaders.SyzygyLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public final class MapWrapped implements Map<String, Object> {

    public static final String UNSUPPORTED_EXPLANATION =
            "The purpose of this class is to provide a way for easily getting configuration in a GUI layer. This " +
                    "method is currently not supported.";
    private final SyzygyLoader config;

    private MapWrapped(SyzygyLoader config) {
        this.config = config;
    }

    /**
     * The purpose of wrapping a syzygy configuration with a map, is to
     * ease query of the elements when used in a rendering layer.
     * @return Configuration wrapped as a map
     * @param config
     */
    public static Map wrapWithMap( SyzygyLoader config ) {
        if ( config == null ) {
            throw new SyzygyException("Usage problem - config parameter cannot be null");
        }

        return new MapWrapped( config );
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
    }

    @Override
    public boolean containsKey(Object key) {
        // This will break if you call a key null.
        return config.lookup(""+key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
    }

    @Override
    public Object get(Object key) {
        Object result = config.lookup(""+key, Object.class);
        if ( result instanceof List && ((List)result).size()==1 ) {
            // Syzygy does not support Lists
            return ((List)result).get(0);
        }
        return result;
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
    }

    @Override
    public Set<String> keySet() {
        return config.keys();
    }

    @Override
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>();
        for ( String key: config.keys()) {
            list.add( config.lookup(key, Object.class));
        }
        return list;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> result = new HashSet<>();
        for ( String key: config.keys()) {
            result.add(new StringEntry(key, config.lookup(key)));
        }
        return result;
    }

    private static class StringEntry implements Entry<String, Object> {

        private final String key;
        private final String value;

        private StringEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException(UNSUPPORTED_EXPLANATION);
        }
    }
}
