package no.api.syzygy.etcd;

import io.fabric8.etcd.api.EtcdClient;
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

    private EtcdConnector(String url) throws URISyntaxException {
        client = new Builder().baseUri(new URI(url)).responseReader(new GsonResponseReader()).build();
        client.start();
        // Just doing a query in order to get exception if etcd is not running
        client.getData().forKey("/");
    }

    public void stop() {
        client.stop();
    }

    public static EtcdConnector attach(String url) {
        try {

            EtcdConnector etcd = new EtcdConnector(url);
            return etcd;

        } catch (Exception e) { // NOSONAR Wide net catch is OK
            log.error("Got exception", e);
        }

        return null;
    }

    /**
     * @return true if storage process was OK
     */
    public boolean store( String key, String value ) {
        Response response = client.setData().value(value).forKey(key);
        if ( response.getErrorCode() != 0 ) {
            log.warn("Got error storing ("+key+", "+value+"). Error code: "+response.getErrorCode());
            return false;
        }
        return true;
    }

    public boolean store( String key, Map<String, Object> map) {

        try {
            client.setData().dir().forKey(key);
        } catch (Exception e) {
            log.debug("Ignoring error, as it probably just is that the directory already exists", e);
            // Confirmation that this is a directory.
            Response directory = client.getData().forKey(key);
            log.debug("Directory exists?? "+directory.getNode().isDir());

        }

        for ( String subkey : map.keySet()) {
            Object obj = map.get(subkey);
            if ( obj instanceof String ) {
                if ( ! store( key+"/"+subkey, (String) obj)) {
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
            response = client.delete().forKey(key);
        } catch (Exception e) {
            log.error("Got exception removing key: "+key, e);
            return false;
        }
        if ( response.getErrorCode() != 0 ) {
            log.warn("Got removing ("+key+"). Error code: "+response.getErrorCode());
            return false;
        }
        return true;
    }

    public Map<String, Object> valueBy(String key, Class clazz ) {
        return null;
    }

    public Object valueBy( String key ) {

        Response data = null;
        try {
            data = client.getData().forKey(key);
        } catch (Exception e) {
            log.warn("Got exception trying to read data with key " + key, e);
            return null;
        }
        if ( data.getNode().isDir()) {
            Map map = new HashMap();
            Response response = client.getData().recursive().forKey(key);
            Set<Node> nodes = response.getNode().getNodes();
            for ( Node n : nodes ) {
                if ( n.isDir() ) {
                    // Nested map
                    map.put(n.getKey(), valueBy(n.getKey()));
                } else {
                    map.put(n.getKey(), n.getValue());
                }
            }
            return map;
        }
        return data.getNode().getValue();
    }


}
