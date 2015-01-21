package no.api.syzygy.etcd;

import no.api.syzygy.ConfigurationProvider;
import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.loaders.SyzygyFileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
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

        return SyzygyEtcdConfig.connectAs( etcd, configurationName);
    }
}
