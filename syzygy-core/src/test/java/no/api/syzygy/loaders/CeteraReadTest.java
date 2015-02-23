package no.api.syzygy.loaders;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CeteraReadTest {
    private static final Logger log = LoggerFactory.getLogger(CeteraReadTest.class);

    private File syzygyfile;

    @Before
    public void setUp() {
        syzygyfile = new File("/etc/api/syzygy/syzygy.yaml");
        Assume.assumeTrue(syzygyfile.exists());
    }


    /**
     * This test is expected to break sooner or later. Just add it to Ignore, if it does.
     * This code is mainly here in order easily test syzygy setup
     */
    @Test
    public void testCeteraRead() {
        SyzygyLoader loader = SyzygyLoader.loadConfigurationFile(syzygyfile);
        assertEquals("value from common", loader.lookup("this.is.just.a.test.value"));
        assertEquals("specific value for www.rb.no", loader.deepLookup("this.is.just.a.test.value", "www.rb.no"));
        assertEquals("value from common", loader.deepLookup("this.is.just.a.test.value", "www.ba.no"));
    }

}
