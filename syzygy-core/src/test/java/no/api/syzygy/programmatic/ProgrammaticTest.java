package no.api.syzygy.programmatic;

import no.api.syzygy.HieradirectoryHelper;
import no.api.syzygy.SyzygyException;
import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class ProgrammaticTest {
    private SyzygyLoader hiera;
    private String readFrom;

    @Before
    public void setUp() {
        readFrom = HieradirectoryHelper.findTestDirectoryReference("programatic");
        hiera = SyzygyLoader.loadConfigurationFile(new File( readFrom+"/dummy.yaml"));
    }

    @Test
    public void testDummyconfig() {
        // Awaiting reimplementation
        assertEquals("dummy", hiera.lookup("dummy"));
        assertEquals("everything is just returned", hiera.lookup("everything is just returned"));
    }

    @Test
    public void testReadingWithError() {
        SyzygyLoader.loadConfigurationFile(new File( readFrom+"/error_is_ignored.yaml"));
        try {
            SyzygyLoader.loadConfigurationFile(new File( readFrom+"/error_is_not_ignored.yaml"));
            fail("Error should have been thrown");
        } catch (SyzygyException expected ) {}
    }

    @Test
    public void testName() {
        SyzygyLoader loader = SyzygyLoader.loadConfigurationFile(new File(readFrom + "/dummy.yaml"));
        assertEquals(1, loader.configurationNames().size());
        assertEquals("Expecting configuration name to be deterministic.", "config_a", loader.configurationNames().get(0));
    }
}
