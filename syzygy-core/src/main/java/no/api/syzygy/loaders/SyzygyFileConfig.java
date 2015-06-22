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

package no.api.syzygy.loaders;

import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of config read from a yaml or json file.
 */
public class SyzygyFileConfig extends AbstractConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(SyzygyFileConfig.class);

    private Map map;

    private URI origin;

    public SyzygyFileConfig(String name) {
        super(name);
    }


    /**
     * For internal use only. Used to hardwire configuration of hierarchical elements.
     */
    protected SyzygyFileConfig(String name, Map map, URI origin) {
        super(name);
        this.map = map;
        this.origin = origin;
    }

    /**
     * For internal use only. Used to hardwire configuration of hierarchical elements.
     */
    protected SyzygyFileConfig(String name, Map map, File origin) {
        this( name, map, origin.toURI());
    }

    protected SyzygyFileConfig load(final String filename ) {
        return load( new File(filename));
    }


    public SyzygyFileConfig load(final File file ) {
        return load( file.toURI() );
    }
    /**
     * Notice that this method might become protected again. Not decided
     */
    public SyzygyFileConfig load(final URI uri ) {
        map = load( uri, createObjectMapper());
        log.debug("Loaded "+uri+" and got "+map.size()+" items (on top level).");
        map.put(SYZYGY_CFG_FILE, uri.getPath());
        origin = uri;
        return this;
    }


    /**
     * Currently not intended to be used from clients
     * @return Unmodifiable map without internal element(s)
     */
    public Map getMap() {
        Map m = new HashMap(map);
        m.remove(SYZYGY_CFG_FILE);
        return Collections.unmodifiableMap(m);
    }

    protected URI getOrigin() {
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
