package no.api.syzygy.etcd;

import io.fabric8.etcd.api.EtcdClient;
import io.fabric8.etcd.api.EtcdException;
import io.fabric8.etcd.api.Node;
import io.fabric8.etcd.api.Response;
import io.fabric8.etcd.core.EtcdClientImpl.Builder;
import io.fabric8.etcd.dsl.DeleteDataBuilder;
import io.fabric8.etcd.reader.jackson.JacksonResponseReader;
import no.api.syzygy.SyzygyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class EtcdConnector {

    private static final Logger log = LoggerFactory.getLogger(EtcdConnector.class);

    private final EtcdClient client;
    private final String prefix;

    private EtcdConnector(String url, String prefix) throws URISyntaxException {
        // We are working within a subdirectory of etcd. Intentionally using variable and not constant
        this.prefix = prefix;
        client = new Builder().baseUri(new URI(url)).responseReader(new JacksonResponseReader()).build();
        client.start();
    }

    /**
     * This would be your default entry point for getting the connector.
     * It will work on the default sub director
     */
    public static EtcdConnector attach(String url) {
        return attach(url, "/syzygy/");
    }

    /**
     * Only use this method if you <b>intentionally</b> need or want
     * a different prefix. <b>This is not the entry point for normal use.</b>
     * @see #attach(String)
     */
    public static EtcdConnector attach(String url, String prefix) {
        if ( !prefix.endsWith("/") || !prefix.startsWith("/")) {
            throw new SyzygyException("Expecting prefix to start and end with a slash (/). It did not - it was: "+prefix);
        }
        if ( !prefix.startsWith("/syzygy")) {
            log.warn("Unexpected prefix: "+prefix+". Note that this may mess up for linpro if this is not intentional. " +
                    "We are sharing access to the etcd structure, and need to stay within /syzygy/");
        }
        try {
            EtcdConnector etcd = new EtcdConnector(url, prefix);

            return etcd.makeReady();
        } catch (Exception e) { // NOSONAR Wide net catch is OK
            log.error("Got exception", e);
        }

        return null;
    }

    /**
     * Stop the etcd client.
     */
    public void stop() {
        int numberOfChildren = numberOfChildElements(""); // "" becomes the prefix itself
        if ( numberOfChildren == 0 ) {
            removeDirectory("", false);
        }
        client.stop();
    }

    /**
     * @return Number of child elements, or <tt>-1</tt> if some error occurred.
     */
    public int numberOfChildElements(String key) {
        try {
            Response data = client.getData().forKey(prefix+key);
            if ( data.getNode().isDir() ) {
                return data.getNode().getNodes().size();
            }
        } catch (EtcdException ignore) {
            log.debug("Ignoring exception, which just indicates that " + prefix + key + " is not a directory");
        }

        return -1;
    }

    /**
     * @return true if storage process was OK
     */
    public boolean store( String key, String value ) {
        Response response = client.setData().value(value).forKey(prefix+key);
        if ( response.getErrorCode() != 0 ) {
            log.warn("Got error storing ("+prefix+key+", "+value+"). Error code: "+response.getErrorCode());
            return false;
        }
        return true;
    }

    /**
     * This method is intended for junit test use only. It is destructive, and if
     * it fails, your data may be in an incorrect state
     */
    public boolean store( String key, Map<String, Object> map) {
        try {
            if ( !isDirectory(key)) {
                log.trace("Creating directory {} as it does not exist already.", prefix + key);
                client.setData().dir().forKey(prefix+key);
            }
        } catch (EtcdException e) {
            log.trace("Ignoring error, as it probably just is that the directory already exists", e);
            // Confirmation that this is a directory.
            Response directory = client.getData().forKey(prefix+key);
            log.trace("Directory exists?? " + directory.getNode().isDir());

        }

        for ( String subkey : map.keySet()) {
            Object obj = map.get(subkey);
            if ( obj instanceof String ) {
                log.trace("Trying to store at {}/{}", prefix + key, subkey);
                // Notice: Without prefix here, as prefix will be added in store
                if (!store(key + "/" + subkey, (String) obj)) {
                    return false;
                }
            } else if ( Map.class.isAssignableFrom(obj.getClass())) {
                return store( key+"/"+subkey, (Map)obj);
            } else {
                throw new SyzygyException("Sorry - I not supporting data type "+obj.getClass().getName()+" yet.");
            }
        }
        return true;
    }

    /**
     * Use with caution, as this will <b>recursively remove your values</b>
     */
    public boolean removeMap(String somemap) {
        if ( !isDirectory(somemap)) {
            return false;
        }
        return removeDirectory(somemap, true);
    }

    public boolean remove(String key) {
        Response response = null;
        try {
            Response data = client.getData().forKey(prefix+key);
            if ( data.getNode().isDir() ) {
                if ( keys(key).size() == 0 ) {
                    return removeDirectory( key, false );
                } else {
                    log.warn("N");
                    return false;
                }
            }
            response = client.delete().forKey(prefix+key);
        } catch (EtcdException e) {
            log.error("Got exception removing key: "+prefix+key, e);
            return false;
        }
        if ( response.getErrorCode() != 0 ) {
            log.warn("Got removing ("+prefix+key+"). Error code: "+response.getErrorCode());
            return false;
        }
        return true;
    }

    public Object  valueBy( String key ) {
        Response data = null;
        try {
            data = client.getData().forKey(prefix+key);
        } catch (EtcdException e) {
            log.warn("Got exception trying to read data with key {}. Masked exception is " + e, prefix+key );
            return null;
        }
        if ( data.getNode().isDir()) {
            log.trace("{} is a directory. Trying to load it as a map.", prefix+key);
            Map map = new HashMap();
            Response response = client.getData().recursive().forKey(prefix+key);
            // So /syzygy/somemap/abc should be named abc
            final int skipPrefix = prefix.length()+key.length()+1; // +1 due to ending slash
            Set<Node> nodes = response.getNode().getNodes();
            log.trace("Prefix to remove when storing in map:  {}. Number of nodes in map: {}", prefix + key,
                    nodes.size());
            for ( Node n : nodes ) {
                // Map will have path /syzygy/somemap/key
                int removeSlash = n.getKey().endsWith("/")
                                ? 1
                                : 0;
                final String k = n.getKey().substring(skipPrefix + removeSlash);
                log.trace("Got node: {} with map key {}", n.getKey(), k);
                if ( n.isDir() ) {
                    // Nested map
                    map.put(k, valueBy(key+"/"+k));
                } else {
                    map.put(k, n.getValue());
                }
            }
            return map;
        }
        return data.getNode().getValue();
    }

    /**
     * Use with caution. With a wrong key / reference, you might remove the
     * wrong dataset.
     */
    public boolean removeDirectory(String key, boolean recursive ) {
        Response response = null;
        try {
            DeleteDataBuilder deleter = client.delete();
            if ( recursive ) {
                deleter = deleter.recursive();
            }
            response = deleter.dir().forKey(prefix+key);
        } catch (EtcdException e) {
            log.error("Got exception removing key: "+prefix+key, e);
            return false;
        }
        if ( response.getErrorCode() != 0 ) {
            log.warn("Got removing ("+prefix+key+"). Error code: "+response.getErrorCode());
            return false;
        }
        return true;
    }

    private boolean isDirectory(String key) {
        try {
            Response data = client.getData().forKey(prefix+key);
            if ( data.getNode().isDir() ) {
                return true;
            }
        } catch (EtcdException ignore) {
            log.trace("Ignoring exception, which just indicates that {} is not a directory", prefix + key);
        }
        return false;
    }

    private EtcdConnector makeReady() {
        // Just doing a query in order to get exception if etcd is not running
        client.getData().forKey("/");

        try {
            client.setData().dir().forKey(prefix);
        } catch (EtcdException e) {
            log.debug("Directory for "+prefix+" does (probably) exist already. Masked exception: "+e);
        }

        return this;
    }

    public Set<String> keys(String name) {
        Set<String> keys = new HashSet<>();
        try {
            Set<Node> nodes = client.getData().forKey(prefix +"/"+name ).getNode().getNodes();
            final int skipPrefix = prefix.length()+name.length();
            for ( Node n : nodes ) {
                keys.add(n.getKey().substring(skipPrefix));
            }
        } catch (EtcdException e) {
            log.debug("Directory for "+prefix+" does (probably) exist. Masked exception: "+e);
        }

        return keys;
    }

    public Map<String,Object> getMap(String name) {
        return (Map<String, Object>) valueBy(name);
    }

    /**
     * @param configName Configuration name / path. I.e. if you do a get in etcd on this name, you get
     *                   a map as return value
     */
    public String syncMapInto(String configName, Map<String,Object> map) {
        String[] akeys = keys(configName).toArray(new String[0]);
        String[] bkeys = map.keySet().toArray(new String[0]);
        Arrays.sort(akeys);
        Arrays.sort(bkeys);

        List keysToRemoveFromEtcd = Arrays.asList(akeys);
        keysToRemoveFromEtcd.removeAll(Arrays.asList(bkeys));

        List keysToAddToEtcd = new ArrayList( Arrays.asList(bkeys));
        keysToAddToEtcd.removeAll(Arrays.asList(bkeys));

        List justCheckThatContentsAreEqual = Arrays.asList(bkeys);
        justCheckThatContentsAreEqual.removeAll( keysToAddToEtcd );

        log.debug("Got "+keysToRemoveFromEtcd.size()+" keys to remove from etcd, "
                +keysToAddToEtcd.size()+" keys to add, and "
                +justCheckThatContentsAreEqual.size()+" which just needs to be checked.");

        return null;
    }
    private void compareMaps(Map<String,Object> a, Map<String,Object> b) {
        String[] akeys = a.keySet().toArray(new String[0]);
        String[] bkeys = b.keySet().toArray(new String[0]);
        Arrays.sort(akeys);
        Arrays.sort(bkeys);
        //assertArrayEquals(akeys,bkeys);
        for ( String key : akeys ) {
            Object aobj = a.get(key);
            Object bobj = a.get(key);
            if ( aobj instanceof Map && bobj instanceof Map ) {
                compareMaps((Map<String,Object>)aobj, (Map<String,Object>)bobj);
            } else {
                //assertEquals(aobj, bobj);
            }
        }

    }

}
