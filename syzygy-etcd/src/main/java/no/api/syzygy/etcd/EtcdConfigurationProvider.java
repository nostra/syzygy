package no.api.syzygy.etcd;

import no.api.syzygy.ConfigurationProvider;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.loaders.SyzygyFileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This load will get the configuration name. It will take that name, and
 *  try to mount it as a separate syzgyy configuration.
 */
public class EtcdConfigurationProvider implements ConfigurationProvider {
    private static final Logger log = LoggerFactory.getLogger(EtcdConfigurationProvider.class);

    @Override
    public SyzygyConfig findConfigurationFrom(SyzygyFileConfig topLevelConfig, String configurationName) {
        String etcdUrl = topLevelConfig.lookup("etcdUrl");
        String prefix = topLevelConfig.lookup("etcdPrefix");
        if ( etcdUrl == null ) {
            etcdUrl = "http://127.0.0.1:4001/v2/";
            log.warn("Expected to find etcdUrl from etcdUrl config element. Using fallback: "+etcdUrl );
        }
        if ( prefix != null && !prefix.startsWith("/syzygy")) {
            log.warn("Expected that etcdPrefix starts with /syzygy/. USE WITH CAUTION.");
        }
        if ( prefix == null ) {
            prefix = "/syzygy/";
        }
        EtcdConnector etcd = EtcdConnector.attach(etcdUrl, prefix);

        SyzygyConfig cfg = SyzygyEtcdConfig.connectAs(etcd, configurationName);
        if (!cfg.keys().isEmpty()) {
            log.debug("Configuration {} was found in etcd and returned", configurationName );
            return cfg;
        }
        log.debug("Configuration {} was not found in etcd", configurationName );
        return null;
    }
}
