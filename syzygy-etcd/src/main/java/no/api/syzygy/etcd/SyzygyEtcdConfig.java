package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;

import java.util.Set;

/**
 * Mount an etcd structure as syzygy config. You would typically mount an
 * etcd sub structure.
 */
public class SyzygyEtcdConfig implements SyzygyConfig {


    private final EtcdConnector etcd;

    private final String name;

    public SyzygyEtcdConfig(EtcdConnector etcd, String name) {
        if ( etcd == null ) {
            throw new SyzygyException("Not allowing etcd be null");
        }
        this.etcd = etcd;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String lookup(String key) {
        return lookup(key, String.class);
    }

    @Override
    public <T> T lookup(String key, Class<T> clazz) {
        Object obj = etcd.valueBy(getName() + "/" + key);
        if  (obj == null ) {
            return null;
        }
        if ( clazz.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }
        // TODO If integer, or some other supported type, convert it.
        throw new SyzygyException("Not implemented conversion from "+obj.getClass().getSimpleName()+" to "
                +clazz.getSimpleName()+" on key "+key);
    }

    @Override
    public Set<String> keys() {
        // Plus / as a wrapped config really lives in a map
        return etcd.keys(getName()+"/");
    }

    /**
     * Mount a subdirectory from the current etcd structure with etcd
     * @param etcd Connected etcd instance. It may have a prefix
     * @param name Last path element, and name of this connection
     */
    public static SyzygyConfig connectAs(EtcdConnector etcd, String name) {
        return new SyzygyEtcdConfig(etcd, name);
    }
}
