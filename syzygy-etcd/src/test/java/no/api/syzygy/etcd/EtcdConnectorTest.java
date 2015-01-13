package no.api.syzygy.etcd;

import io.fabric8.etcd.api.EtcdClient;
import io.fabric8.etcd.api.EtcdException;
import io.fabric8.etcd.api.Response;
import io.fabric8.etcd.core.EtcdClientImpl.Builder;
import io.fabric8.etcd.reader.gson.GsonResponseReader;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class EtcdConnectorTest {

    private EtcdClient client;

    @Before
    public void setUp() throws URISyntaxException {
        Assume.assumeTrue(false); // Deactivating test

        /***
         *
         * NOTE
         *
         * These tests are copied from fabric8, and are just here in order
         * to be used as documentation.
         *
         *
         */


        String url = "http://127.0.0.1:4001/v2/"; // System.getProperty("etcd.url");
        client = new Builder().baseUri(new URI(url)).responseReader(new GsonResponseReader()).build();
        client.start();
        try {
            client.delete().dir().recursive().forKey("key");
        } catch (EtcdException e) {
            //ignore
        }

    }

    @After
    public void tearDown() {
        if ( client == null ) {
            return;
        }
        try {
            client.delete().forKey("key");
        } catch (EtcdException e) {
            //ignore
        } finally {
            client.stop();
        }
    }

    @Test
    public void testConnectToEtcd() throws Exception {
        // client.setData().dir().forKey("/syzygy").;
    }

    @Test
    public void testSetGetAndDelete() {
        Response response = client.setData().value("smoke").forKey("key");
        assertNotNull(response);
        assertEquals("smoke", response.getNode().getValue());

        response = client.getData().forKey("key");
        assertValue("smoke", response);

        response = client.delete().forKey("key");
        assertPrevValue("smoke", response);
    }

    @Test(expected = TimeoutException.class)
    public void testSetWatchWithTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        Response response = client.setData().value("smoke").forKey("key");
        assertNotNull(response);
        assertEquals("smoke", response.getNode().getValue());

        response = client.getData().forKey("key");
        assertValue("smoke", response);

        Future<Response> responseFuture = client.getData().watch().forKey("key");
        responseFuture.get(3, TimeUnit.SECONDS);
    }

    @Test
    public void testSetWatchAndDelete() throws ExecutionException, InterruptedException {
        Response response = client.setData().value("smoke").forKey("key");
        assertNotNull(response);
        assertEquals("smoke", response.getNode().getValue());

        response = client.getData().forKey("key");
        assertValue("smoke", response);

        Future<Response> responseFuture = client.getData().watch().forKey("key");

        response = client.setData().value("smoke updated").forKey("key");
        assertValue("smoke updated", response);
        assertPrevValue("smoke", response);

        response = responseFuture.get();
        assertValue("smoke updated", response);
        assertPrevValue("smoke", response);

        response = client.delete().forKey("key");
        assertPrevValue("smoke updated", response);
    }

    @Test
    public void testCreateDirAndChildren() throws URISyntaxException {
        Response response = client.setData().dir().forKey("key");
        assertNotNull(response);

        response = client.setData().value("value1").forKey("key/child1");
        assertNotNull(response);
        assertEquals("value1", response.getNode().getValue());
        response = client.setData().value("value2").forKey("key/clild2");
        assertNotNull(response);
        assertEquals("value2", response.getNode().getValue());

        response = client.getData().recursive().forKey("key/");
        assertNotNull(response);
        assertNotNull(response.getNode().getNodes());
    }

    @Test
    public void testSetGetAndDeleteWithPrevValues() throws URISyntaxException {
        Response response = client.setData().value("smoke").forKey("key");
        assertNotNull(response);
        assertEquals("smoke", response.getNode().getValue());

        response = client.getData().forKey("key");
        assertValue("smoke", response);

        //Set with prev value
        try {
            client.setData().value("smoke updated").prevValue("wrong").forKey("key");
        } catch (EtcdException e) {
            assertEquals(101, e.getErrorCode());
        }

        response = client.setData().value("smoke updated").prevValue("smoke").forKey("key");
        assertPrevValue("smoke", response);

        response = client.delete().forKey("key");
        assertPrevValue("smoke updated", response);
    }

    void assertValue(String expected, Response response) {
        assertNotNull(response);
        assertNotNull(response.getNode());
        assertEquals(expected, response.getNode().getValue());
    }

    void assertPrevValue(String expected, Response response) {
        assertNotNull(response);
        assertNotNull(response.getPrevNode());
        assertEquals(expected, response.getPrevNode().getValue());
    }


}