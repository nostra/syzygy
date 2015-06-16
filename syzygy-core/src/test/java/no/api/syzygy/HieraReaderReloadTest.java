package no.api.syzygy;

import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HieraReaderReloadTest {
    private SyzygyLoader hiera;
    private File tmp;
    private File hieracfg;

    @Before
    public void setUp() throws IOException {
        //tmp = File.createTempFile("syzygy_junit", ".yaml");
        tmp = new File("/tmp/syzygy_junit.yaml");
        assertTrue(write("key1: 'initial value'", tmp));

        hieracfg = new File("/tmp/syzygy.yaml");
        write( ":backends:\n" +
                "  - yaml\n" +
                ":yaml:\n" +
                "  :datadir: \n" +
                ":hierarchy:\n" +
                "  - syzygy_junit\n", hieracfg );
        hiera = SyzygyLoader.loadConfigurationFile(hieracfg);

    }

    @After
    public void tearDown() {
        tmp.deleteOnExit();
        hieracfg.deleteOnExit();
    }

    @Test
    public void testReload() throws IOException {
        assertEquals("initial value", hiera.lookup("key1"));
        assertTrue(write("key1: 'updated value'", tmp));

        hiera.flush();

        assertEquals("updated value", hiera.lookup("key1"));
    }

    private boolean write(String content, File tmp) throws IOException {
        try ( Writer writer = new FileWriter( tmp ) ){
            writer.write(content);
            writer.flush();
            return true;
        }
    }
}
