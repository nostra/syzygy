package no.api.syzygy;

import no.api.syzygy.loaders.SyzygyFileConfig;

/**
 *
 */
public interface ConfigurationProvider {

    SyzygyConfig findConfigurationFrom( SyzygyFileConfig topLevelConfig, String configurationName );
}
