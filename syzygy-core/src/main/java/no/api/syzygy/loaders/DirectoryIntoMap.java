package no.api.syzygy.loaders;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyDynamicLoader;
import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Based on the <tt>directory_to_map</tt> configuration element, load
 * all files in sub directory. Intended for doing things like:
 * <code>
 *     syzygy.deepLookup("somekey", "www.ba.no")
 * </code>
 * where <tt>somekey</tt> is overridden in www.ba.no.yaml as read from a configured directory.
 */
public class DirectoryIntoMap implements SyzygyDynamicLoader {
    private static final Logger log = LoggerFactory.getLogger(DirectoryIntoMap.class);

    @Override
    public SyzygyConfig createSyzygyConfigWith(String configurationString, SyzygyConfig loaderConfiguration) {
        String baseDirectory = new File( loaderConfiguration.lookup(SyzygyConfig.SYZYGY_CFG_FILE)).getParent();
        String configKey     = configurationString+"_directory_to_map";
        String subdirectory = loaderConfiguration.lookup(configKey);
        if ( subdirectory == null ) {
            throw new SyzygyException("You need to specify '"+configKey+"' in the top level configuration, " +
                    "i.e. in this file: "+loaderConfiguration.lookup(SyzygyConfig.SYZYGY_CFG_FILE));
        }
        File directoryToTraverse = new File(baseDirectory+File.separator+subdirectory);
        if ( !directoryToTraverse.exists() || !directoryToTraverse.isDirectory() ) {
            throw new SyzygyException("Directory given to traverse does not exist: "+directoryToTraverse);
        }

        Map<String, Map> map = new HashMap();
        for ( File file : directoryToTraverse.listFiles()) {
            SyzygyFileConfig cfg = new SyzygyFileConfig( stripExt( file.getName()) ).load(file);
            log.trace("Loaded " + file + " into configuration: " + cfg.getName() + ", got " + cfg.getMap().size() +
                    " elements.");
            map.put( cfg.getName(), cfg.getMap());
        }

        return new SyzygyFileConfig(getClass().getSimpleName(), map, directoryToTraverse);
    }

    private String stripExt(String name) {
        int dot = name.lastIndexOf(".");
        if ( dot != -1 ) {
            return name.substring(0, dot);
        }
        return name;
    }
}
