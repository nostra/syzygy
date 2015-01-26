package no.api.syzygy;

/**
 * Provide an alternative of reading configuration data. It will get a configuration name, and
 * should return a SyzygyConfig if it is able to provide one, and null otherwise.
 */
public interface ConfigurationProvider {

    /**
     * @param topLevelConfig Top level configuration which may be used in order to obtain configuration detail
     * @param configurationName Name of the configuration which this provider might, eh, provide
     * @return null if none, or and instance of SyzygyConfig
     */
    SyzygyConfig findConfigurationFrom( SyzygyConfig topLevelConfig, String configurationName );
}
