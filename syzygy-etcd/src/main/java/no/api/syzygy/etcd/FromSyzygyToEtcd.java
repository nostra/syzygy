package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;

import java.util.Map;

/**
 * WARNING: Not sure if we should continue to support this class
 */
public class FromSyzygyToEtcd {

    private EtcdConnector etcdConnector;

    /**
     * NOTE that this is additive - it does not remove elements not present in etcd.
     */
    public static void storeConfigInto(SyzygyConfig syzgyConfig, EtcdConnector etcdConnector) {
        new FromSyzygyToEtcd( etcdConnector ).mapFrom( syzgyConfig );
    }

    public FromSyzygyToEtcd(EtcdConnector etcdConnector) {
        this.etcdConnector = etcdConnector;
    }

    private void mapFrom(SyzygyConfig syzgyConfig) {
        final String prefix = syzgyConfig.getName()+"/";
        int counter = 0;
        for ( String key : syzgyConfig.keys()) {
            String translatedKey = prefix+key;
            counter++;
            Object obj = syzgyConfig.lookup(key, Object.class);
            boolean storedOk = false;
            if ( obj instanceof Map) {
                storedOk = etcdConnector.store(translatedKey, (Map)obj);
            } else if ( obj instanceof String) {
                storedOk = etcdConnector.store(translatedKey, (String)obj);
            } else {
                throw new SyzygyException("Unexpected data type in syzygy. Got: "+obj.getClass());
            }
            if ( !storedOk ) {
                throw new SyzygyException("Trouble storing element with key "+translatedKey+". This was item "+counter
                        +" out of "+syzgyConfig.keys().size());
            }
        }
    }

}
