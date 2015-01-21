package no.api.syzygy.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import no.api.pantheon.io.FileUtils;
import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of config from a yaml or json file.
 */
public class SyzygyFileConfig extends AbstractConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(SyzygyFileConfig.class);

    private Map map;

    private File origin;

    public SyzygyFileConfig(String name) {
        super(name);
    }


    /**
     * For internal use only. Used to hardwire configuration of hierarchical elements.
     */
    protected SyzygyFileConfig(String name, Map map, File origin) {
        super(name);
        this.map = map;
        this.origin = origin;
    }

    protected SyzygyFileConfig load(final String filename ) {
        return load( new File(filename));
    }


    protected SyzygyFileConfig load(final File file ) {
        map = load( file, new ObjectMapper(new YAMLFactory()));
        log.debug("Loaded "+file+" and got "+map.size()+" items.");
        map.put(SYZYGY_CFG_FILE, FileUtils.canonicalPathOf(file));
        origin = file;
        return this;
    }


    /** Temporarily public */
    public Map getMap() {
        return Collections.unmodifiableMap(map);
    }

    protected File getOrigin() {
        return origin;
    }

    @Override
    public String lookup(String key) {
        return lookup(key, String.class);
    }

    @Override
    public <T> T lookup(String key, Class<T> clazz) {
        Object node = map.get(key);
        if ( node == null ) {
            return null;
        }
        if ( node instanceof List && !clazz.isAssignableFrom(List.class) && ((List)node).size() == 1 ) {
            // If you are looking for something which is in a list of size 1
            node = ((List)node).get(0);
        }
        if ( !clazz.isAssignableFrom(node.getClass())) {
            throw new SyzygyException("Lookup on key: "+key+". Specified type was "+clazz.getName()
                    +", whereas the type of the result is "+node.getClass().getName()+". toString(): "+node);
        }
        try {
            return clazz.cast(node);
        } catch(ClassCastException e) {
            throw new SyzygyException("Unexpected type. Could not cast "+node.getClass().getName()+" to "+clazz.getName(), e);
        }
    }

    @Override
    public Set keys() {
        return map.keySet();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SyzygyFileConfig)) {
            return false;
        }

        SyzygyFileConfig other = (SyzygyFileConfig) obj;

        return getName().equals(other.getName()) && map.equals(other.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode()+getName().hashCode();
    }

}
