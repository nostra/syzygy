package no.api.syzygy.etcd;

import io.fabric8.etcd.api.EtcdClient;
import io.fabric8.etcd.api.EtcdException;
import io.fabric8.etcd.api.Node;
import io.fabric8.etcd.api.Response;
import io.fabric8.etcd.core.EtcdClientImpl.Builder;
import io.fabric8.etcd.reader.gson.GsonResponseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class EtcdConnector {

    private static final Logger log = LoggerFactory.getLogger(EtcdConnector.class);

    private final EtcdClient client;
    private final String prefix;

    private EtcdConnector(String url) throws URISyntaxException {
        this(url, "/syzygy/");
    }

    private EtcdConnector(String url, String prefix) throws URISyntaxException {
        // We are working within a subdirectory of etcd. Intentionally using variable and not constant
        this.prefix = prefix;
        client = new Builder().baseUri(new URI(url)).responseReader(new GsonResponseReader()).build();
        client.start();
    }

    public void stop() {
        client.stop();
    }

    public static EtcdConnector attach(String url) {
        try {
            EtcdConnector etcd = new EtcdConnector(url);

            return etcd.makeReady();
        } catch (Exception e) { // NOSONAR Wide net catch is OK
            log.error("Got exception", e);
        }

        return null;
    }

    private EtcdConnector makeReady() {
        // Just doing a query in order to get exception if etcd is not running
        client.getData().forKey("/");

        try {
            client.setData().dir().forKey(prefix);
        } catch (Exception e) {
            log.debug("Directory for "+prefix+" does (probably) exist already. Masked exception: "+e);
        }

        return this;
    }

    /**
     * @return true if storage process was OK
     */
    public boolean store( String key, String value ) {
        Response response = client.setData().value(value).forKey(key);
        if ( response.getErrorCode() != 0 ) {
            log.warn("Got error storing ("+prefix+key+", "+value+"). Error code: "+response.getErrorCode());
            return false;
        }
        return true;
    }

    public boolean store( String key, Map<String, Object> map) {
        try {
            //if ( client.getData().dir().)
            client.setData().dir().forKey(prefix+key);
        } catch (Exception e) {
            log.debug("Ignoring error, as it probably just is that the directory already exists", e);
            // Confirmation that this is a directory.
            Response directory = client.getData().forKey(prefix+key);
            log.debug("Directory exists?? "+directory.getNode().isDir());

        }

        for ( String subkey : map.keySet()) {
            Object obj = map.get(subkey);
            if ( obj instanceof String ) {
                if ( ! store( prefix+key+"/"+subkey, (String) obj)) {
                    return false;
                }
            } else {
                throw new RuntimeException("Sorry - I not supporting data type "+obj.getClass().getName()+" yet.");
            }
        }
        return true;
    }

    public boolean remove(String key) {
        Response response = null;
        try {
            Response data = client.getData().forKey(prefix+key);
            if ( data.getNode().isDir() ) {
                return removeDirectory( prefix+key );
            }
            response = client.delete().forKey(prefix+key);
        } catch (Exception e) {
            log.error("Got exception removing key: "+prefix+key, e);
            return false;
        }
        if ( response.getErrorCode() != 0 ) {
            log.warn("Got removing ("+prefix+key+"). Error code: "+response.getErrorCode());
            return false;
        }
        return true;
    }

    private boolean removeDirectory(String key) {
        Response response = null;
        try {
            response = client.delete().dir().forKey(prefix+key);
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

    public Object valueBy( String key ) {
        Response data = null;
        try {
            data = client.getData().forKey(prefix+key);
        } catch (Exception e) {
            log.warn("Got exception trying to read data with key " + prefix+key, e);
            return null;
        }
        if ( data.getNode().isDir()) {
            Map map = new HashMap();
            Response response = client.getData().recursive().forKey(prefix+key);
            // So /syzygy/somemap/abc should be named abc
            final int skipPrefix = prefix.length()+2+key.length();
            Set<Node> nodes = response.getNode().getNodes();
            for ( Node n : nodes ) {
                // Map will have path /syzygy/somemap/key
                final String k = n.getKey().substring(skipPrefix);
                if ( n.isDir() ) {
                    // Nested map
                    map.put(k, valueBy(n.getKey()));
                } else {
                    map.put(k, n.getValue());
                }
            }
            return map;
        }
        return data.getNode().getValue();
    }

}
