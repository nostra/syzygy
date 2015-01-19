package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;

import java.util.Map;

/**
 *
 */
public class FromSyzygyToEtcd {

    private EtcdConnector etcdConnector;

    public FromSyzygyToEtcd(EtcdConnector etcdConnector) {

        this.etcdConnector = etcdConnector;
    }

    public static void mapSyzygyInto(SyzygyConfig syzgyConfig, EtcdConnector etcdConnector) {
        new FromSyzygyToEtcd( etcdConnector ).mapFrom( syzgyConfig );
    }

    private void mapFrom(SyzygyConfig syzgyConfig) {
        int counter = 0;
        for ( String key : syzgyConfig.keys()) {
            counter++;
            Object obj = syzgyConfig.lookup(key, Object.class);
            boolean storedOk = false;
            if ( obj instanceof Map) {
                storedOk = etcdConnector.store(key, (Map)obj);
            } else if ( obj instanceof String) {
                storedOk = etcdConnector.store(key, (String)obj);
            } else {
                throw new SyzygyException("Unexpected data type in syzygy. Got: "+obj.getClass());
            }
            if ( !storedOk ) {
                throw new SyzygyException("Trouble storing element with key "+key+". This was item "+counter
                        +" out of "+syzgyConfig.keys().size());
            }
        }
    }

}
