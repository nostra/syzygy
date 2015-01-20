package no.api.syzygy.etcd;

import no.api.syzygy.SyzygyConfig;
import no.api.syzygy.SyzygyException;

import java.util.Set;

/**
 * Load a structure with etcd
 */
public class SyzygyEtcdConfig implements SyzygyConfig {


    private final EtcdConnector etcd;

    private final String name;

    public SyzygyEtcdConfig(EtcdConnector etcd, String name) {
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
        if ( obj.getClass().isAssignableFrom(clazz)) {
            return (T) obj;
        }
        // TODO If integer, or some other supported type, convert it.
        throw new SyzygyException("Not implemented conversion from "+obj.getClass().getSimpleName()+" to "
                +clazz.getSimpleName()+" on key "+key);
    }

    @Override
    public Set<String> keys() {
        return etcd.keys(getName());
    }

    public static SyzygyConfig connectAs(EtcdConnector etcd, String name) {
        return new SyzygyEtcdConfig(etcd, name);
    }
}
