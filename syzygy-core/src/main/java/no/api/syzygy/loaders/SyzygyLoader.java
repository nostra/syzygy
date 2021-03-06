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

import no.api.syzygy.ConfigurationProvider;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyDynamicLoader;
import no.api.syzygy.SyzygyException;
import no.api.syzygy.SyzygyPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Load and retain syzygy configuration
 */
public class SyzygyLoader {
    private static Logger log = LoggerFactory.getLogger(SyzygyLoader.class);

    private List<SyzygyConfig> configsets;

    private final SyzygyFileConfig topLevelConfig;

    private final Map<String,String> doc = new HashMap<>();

    private SyzygyLoader(SyzygyFileConfig topLevelConfig) {
        // In order to reload on this level, support exchanging the top level config with new config.
        this.topLevelConfig = topLevelConfig;
    }

    /**
     * Convenience method for loading configuration in the Amedia environment
     * @see #loadConfigurationFile(File)
     */
    public static SyzygyLoader loadConfigurationFromEtcApi() {
        File syzygyFile = new File(System.getProperty("V3_CONFIG_HOME", "/etc/api")+"/syzygy/syzygy.yaml");
        if ( syzygyFile.exists() ) {
            return SyzygyLoader.loadConfigurationFile(syzygyFile);
        } else {
            throw new SyzygyException("Cannot find syzygy configuration file in "+syzygyFile);
        }
    }

    /**
     * Default way of loading syzygy configuration for API users of this API
     * @return Configuration, after loading it from config file
     */
    public static SyzygyLoader loadConfigurationFile( File configFile ) {
        if ( ! configFile.exists() || !configFile.canRead()) {
            throw new SyzygyException("Configuration file does not exist. Tried to read: "+configFile);
        }
        SyzygyLoader loader =
                new SyzygyLoader(
                        new SyzygyFileConfig(configFile.getName())
                                .load(configFile.getPath())
        );
        return loader.reloadHierarchy();
    }

    /**
     * Regular query for string value
     */
    public String lookup(String key) {
        return lookup(key, String.class);
    }

    /**
     * Lookup for key with a special class - typically a map or an integer
     */
    public <T> T lookup(String key, Class<T> clazz) {
        SyzygyPayload<T> result = lookupFor(key, clazz);
        return result == null
                ? null
                : result.getValue();
    }

    /**
     * @return The first configuration which has the given name. This represents a single configuration set.
     */
    public SyzygyConfig configurationWithName(String name) {
        for ( SyzygyConfig conf : configsets ) {
            if ( conf.getName().equals(name)) {
                return conf;
            }
        }
        return null;
    }

    /**
     * @return A list of names for the configuration
     */
    public List<String> configurationNames() {
        List<String> names = new ArrayList<>();
        for ( SyzygyConfig conf : configsets ) {
            names.add(conf.getName());
        }
        return names;
    }

    /**
     * @return The composite collection of keys, i.e. the set of keys from all configurations combined
     */
    public Set<String> keys() {
        Set<String> keys = new HashSet<>();
        for ( SyzygyConfig conf : configsets ) {
            keys.addAll(conf.keys());
        }
        return keys;
    }

    /**
     * Return payload object which contains meta information about usage and
     * path to object.
     */
    public <T> SyzygyPayload<T> lookupFor(String key, Class<T> clazz) {
        return lookupFor(key, clazz, null);
    }

    /**
     *
     * @return A list of @{link SyzygyPayload} containing key, object, and name of config
     */
    public List<SyzygyPayload> listAllProperties() {
        List<SyzygyPayload> all = new ArrayList<>();
        for ( SyzygyConfig conf : configsets ) {
            List<String> name = new ArrayList();
            name.add(conf.getName());
            for ( String key: conf.keys() ){
                if ( keyIsNotInternal( key) ) {
                    all.add(new SyzygyPayload(key, conf.lookup(key,Object.class),name, doc.get(key)));
                }
            }
        }

        return all;
    }

    /**
     * For each configuration file, first try lookup in map.
     * <b>Note</b> that if the value exists in both a map and in the top level configuration
     * for a single configuration set, the mapped value will be used.
     * @param key Which key to look up
     * @param nameOfMap Typically www.rb.no (or similar)
     * @return First element found as string. If overriding map is not found, use plain lookup as fallback
     */
    public String deepLookup(String key, String nameOfMap) {
        SyzygyPayload<String> result = lookupFor(key, String.class, nameOfMap);
        if ( result == null ) {
            // If overriding map does not exist, use plain lookup
            result = lookupFor( key, String.class, null);
        }
        return result == null
                ? null
                : result.getValue();
    }

    /**
     * Does not (at the moment) reload top level config. Reloads all sub-elements.
     */
    public void flush() {
        log.info("Flushing hierarchy. Notice that top level configuration (i.e. the loader's configuration) is NOT reloaded.");
        reloadHierarchy();
    }

    /**
     * List validation errors on log WARN. If configured to stop on error, throw SyzygyException
     */
    public void validate() {
        List<String> errs = new ArrayList<>();
        for ( SyzygyConvictSchemaConfig convict : extractConvictSchemas() ) {
            // Checking all config against convict schema
            for ( SyzygyConfig cfg : configsets ) {
                convict.validate( cfg, errs );
            }
        }
        if (!errs.isEmpty()) {
            for ( String err : errs ) {
                log.warn("Validation error: "+err);
            }
            if (  shouldStopOnError()) {
                throw new SyzygyException(errs.size()+" validation error(s) present: "+errs);
            }
        }
    }

    private boolean keyIsNotInternal(String key) {
        switch (key) {
            case SyzygyConfig.SYZYGY_CFG_FILE :
                return false;
            default:
                return true;
        }
    }

    private <T> SyzygyPayload<T> lookupFor(String key, Class<T> clazz, String nameOfMap) {
        if ( configsets == null ) {
            throw new SyzygyException("No configuration sets present.");
        }
        List<String> path = new ArrayList<>();
        for ( SyzygyConfig conf : configsets ) {
            path.add( conf.getName() );
            T value = mapExistsInConfig( nameOfMap, conf )
                    ? (T) conf.lookup(nameOfMap, Map.class).get(key)
                    : conf.lookup(key, clazz );
            if ( value != null ) {
                return new SyzygyPayload<>(conf.getName(), value, path, doc.get(key));
            }
        }
        return null;
    }

    private boolean mapExistsInConfig(String nameOfMap, SyzygyConfig conf) {
        if ( nameOfMap != null ) {
            Object potentialMap = conf.lookup(nameOfMap, Object.class);
            if ( potentialMap instanceof Map ) {
                return true;
            }
        }
        return false;
    }

    private List<SyzygyConvictSchemaConfig> extractConvictSchemas() {
        List<SyzygyConvictSchemaConfig> convicts = new ArrayList<>();
        for ( SyzygyConfig conf : configsets ) {
            if ( conf instanceof SyzygyConvictSchemaConfig) {
                convicts.add((SyzygyConvictSchemaConfig) conf);
            }
        }
        return convicts;
    }

    private SyzygyLoader reloadHierarchy() {
        List<SyzygyConfig> configs= new ArrayList<>();
        for ( Object elem : topLevelConfig.lookup(":hierarchy", List.class)) {
            SyzygyConfig config = readFromBackend(elem.toString());
            if ( config != null ) {
                configs.add(config);
            }
        }
        configsets = configs;

        // Just calling it in order to see if this generates exception. If it does, it means that the
        // variable is not correctly named
        shouldStopOnError();

        return this;
    }

    private SyzygyConfig readFromBackend(String name) {
        SyzygyDynamicLoader dynamic = (SyzygyDynamicLoader) tenativelyInstantiate(name);
        if ( dynamic != null ) {
            return dynamic.createSyzygyConfigWith(name.substring(name.indexOf("#") + 1), topLevelConfig);
        }
        List<String> datadir = topLevelConfig.lookup(":datadir", List.class);
        if ( datadir == null ) {
            // Same directory as syzygy.yaml
             SyzygyConfig cfg = readHieraFromFile(".", name);
            if ( cfg == null && shouldStopOnError()) {
                throw new SyzygyException("Could not find a file called neither "+name+".yaml, "+name+".json nor a class with name "+name+".");
            }
            return cfg;
        }

        // The data source elements are read consecutively. It is an usage error if the same name occurs
        // in both types of configuration
        SyzygyConfig found = null;
        for ( String dir : datadir ) {
            SyzygyConfig sc;

            ConfigurationProvider dynamicConfiguration = (ConfigurationProvider) tenativelyInstantiate(dir);
            if ( dynamicConfiguration != null ) {
                sc = dynamicConfiguration.findConfigurationFrom(topLevelConfig, name);
            } else {
                sc = readHieraFromFile(dir, name);
            }
            if ( sc != null ) {
                if ( found != null ) {
                    throw new SyzygyException("The configuration name {} is duplicated in configurations. This is not allowed. Configurations: "+datadir);
                }
                found = sc;
            }
        }
        // If found is null, it might be due to automatic config of FQDN or similar.

        return found;
    }

    /**
     * Reading config file. Can read in yaml and json. The latter in json format and convict.js format.
     *
     * @link http://docs.oracle.com/javase/8/docs/api/java/net/URI.html
     * @return Read config, or null if errors are ignored
     */
    private SyzygyConfig readHieraFromFile(String datadir, String name) {
        final String scheme = topLevelConfig.getOrigin().getScheme();
        log.info("Scheme:             "+topLevelConfig.getOrigin().getScheme());
        log.info("SchemeSpecificPart: "+topLevelConfig.getOrigin().getSchemeSpecificPart());
        log.info("Fragment:           "+topLevelConfig.getOrigin().getFragment());
        String origin = topLevelConfig.getOrigin().getSchemeSpecificPart();
        int until = origin.lastIndexOf("/");
        if ( until == -1 ) {
            until = origin.length();
        }
        String filename =
                origin.substring(0, until)
                + File.separator+datadir+File.separator+name;
        String extension = ".yaml";
        if ( ! uriExistsAndCanBeRead(scheme, filename + extension)) {
            extension = ".yml";
        }
        if ( ! uriExistsAndCanBeRead(scheme, filename + extension)) {
            extension = ".json";
        }
        if ( ! uriExistsAndCanBeRead(scheme, filename + extension)) {
            log.warn("Could not find file "+filename+extension+", and returns null for this configuration");
            return null;
        }
        SyzygyConfig cfg = null;
        try {
            cfg = new SyzygyFileConfig(name).load(new URI( scheme, filename + extension, null));
        } catch (URISyntaxException e) {
            throw new SyzygyException("Create URI element out of scheme "+scheme+" and "+filename + extension, e);
        }
        if ( isConfigConvictSchema( cfg )) {
            cfg = new SyzygyConvictSchemaConfig(name).load(filename + extension );
            // Not very elegant to cast right after initialization. Disregarding this ATM
            appendToDocumentation((SyzygyConvictSchemaConfig) cfg);
        }
        return cfg;
    }

    private boolean uriExistsAndCanBeRead(String scheme, String filename) {
        if ( "file".equalsIgnoreCase( scheme )) {
            return new File(filename).exists();
        }
        try (InputStream is = new URI(scheme, filename, null).toURL().openStream()) {
            return is.read() != -1;

        } catch (URISyntaxException | IOException ignored) {} // NOSONAR This is expected, as we will often look up non-existing paths
        return false;
    }

    private void appendToDocumentation(SyzygyConvictSchemaConfig cfg) {
        for ( String key : cfg.keys() ) {
            String documentation = cfg.doc( key );
            if ( documentation != null ) {
                doc.put(key, documentation);
            }
        }
    }

    /**
     * Defaulting to stop on error
     */
    private Boolean shouldStopOnError() {
        Boolean stopIfError = topLevelConfig.lookup("stop_if_error", Boolean.class);
        return stopIfError == null || stopIfError.booleanValue();
    }

    /**
     * Assuming format to be convict, if first element is a map, containing
     * a "default" value.
     */
    private boolean isConfigConvictSchema( SyzygyConfig cfg ) {
        if ( cfg.keys() == null || cfg.keys().isEmpty()) {
            return false;
        }
        Object first = cfg.lookup((String) cfg.keys().iterator().next(), Object.class);
        if ( first instanceof Map && ((Map)first).get("default") != null ) {
            return true;
        }
        return false;
    }

    /**
     * @return Instance of SyzygyDynamicLoader potentionalClass if <code>potentialClass</code> can be instantiated
     */
    private Object tenativelyInstantiate(String potentialClass) {
        int hash = potentialClass.indexOf("#");
        String realclass = hash != -1
                        ? potentialClass.substring(0, hash)
                        : potentialClass;
        try {
            return Class.forName(realclass).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ignored ) { // NOSONAR Acceptable
            // Ignored
        }
        return null;
    }
}
