package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyDynamicLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class EtcdDynamicConfiguration implements SyzygyDynamicLoader {
    private static final Logger log = LoggerFactory.getLogger(EtcdDynamicConfiguration.class);

    @Override
    public SyzygyConfig createSyzygyConfigWith(String refkey, SyzygyConfig loaderConfiguration) {
        String etcdUrl = loaderConfiguration.lookup(refkey+"_etcd_url");
        String prefix = loaderConfiguration.lookup(refkey+"_etcd_prefix");
        if ( etcdUrl == null ) {
            etcdUrl = "http://127.0.0.1:4001/v2/";
            log.warn("Expected to find etcd url from "+refkey+"_etcd_url config element. Using fallback: "+etcdUrl );
        }
        if ( prefix != null && !prefix.startsWith("/syzygy")) {
            log.warn("Expected that etcdPrefix starts with /syzygy/. USE WITH CAUTION. " +
                             "Config key: "+refkey+"_etcd_prefix");
        }
        if ( prefix == null ) {
            prefix = "/syzygy/";
        }
        EtcdConnector etcd = EtcdConnector.attach(etcdUrl, prefix);

        return SyzygyEtcdConfig.connectAs(etcd, refkey);
    }
}
