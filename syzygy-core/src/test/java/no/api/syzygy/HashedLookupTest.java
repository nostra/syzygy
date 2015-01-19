package no.api.syzygy;

import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 */
public class HashedLookupTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());


    private SyzygyLoader syzygy;

    @Before
    public void setUp() {
        String basedir = HieradirectoryHelper.findTestDirectoryReference("hashed");
        syzygy = SyzygyLoader.loadConfigurationFile(new File( basedir + File.separator + "hashed.yaml"));
        syzygy.validate();
    }

    /**
     * Just checking that the contents of the file is as expected and deterministic.
     */
    @Test
    public void testBasicContents() {
        assertEquals( "Not overridden", syzygy.lookup("key3"));
        assertNull(syzygy.lookup("www.ba.no", Map.class).get("key3"));
        assertEquals("value2 for ba", syzygy.lookup("www.ba.no", Map.class).get("key2"));
    }

    @Test
    public void testDifferenceBetweenDeepAndMapLookup() {
        assertNull("This value will become null, as you _first_ find the overriden hashmap, and _then_ " +
                "tries to find the key within in.", syzygy.lookup("www.rb.no", Map.class).get("key1"));
        assertEquals("This lookup works better, as it will take both map and override into consideration",
                "value1 for rb", syzygy.deepLookup("key1", "www.rb.no"));


    }

    @Test
    public void testMappedLookup() {
        assertEquals("value1 for rb", syzygy.deepLookup("key1", "www.rb.no"));
        assertNull(syzygy.deepLookup("key2", "www.rb.no"));
        assertEquals("value2 for ba", syzygy.deepLookup("key2", "www.ba.no"));
        assertEquals("Value should be chosen from top level", "Not overridden", syzygy.deepLookup("key3", "www.ba.no"));
        assertEquals("Value should be chosen from top level", "top.level.config.value", syzygy.lookup("config.key"));
        assertEquals("Value should be chosen from overridden element", "config overridden by rb", syzygy.deepLookup("config.key",
                "www.rb.no"));
    }

    @Test
    public void testValueFromSubdir() {
        assertEquals("This overridden element is found in overrides directory", "value4 for nordlys", syzygy.deepLookup("key4",
                "www.nordlys.no"));
        assertEquals("This overridden element is found in overrides directory", "value5 for nordlys", syzygy.deepLookup("key5",
                "www.nordlys.no"));
        assertEquals("This overridden element is found in overrides directory", "value5 for rb", syzygy.deepLookup("key5",
                "www.rb.no"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SyzygyHelper.printConfigTo(syzygy.listAllProperties(), new OutputStreamWriter(baos));
        String allConf = baos.toString();
        assertTrue(!allConf.isEmpty());
        log.debug("\n" + allConf);
    }

}
