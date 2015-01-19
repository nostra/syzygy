package no.api.syzygy;

import java.util.Set;

/**
 *
 */
public interface SyzygyConfig {

    String SYZYGY_CFG_FILE = "_internal_syzygy_cfg_file_";

    /**
     * @return Name of this configuration service used for reporting purposes
     */
    String getName();

    /**
     * @return Value as a string
     */
    String lookup(String key);

    <T> T lookup(String key, Class<T> clazz);

    Set<String> keys();
}
