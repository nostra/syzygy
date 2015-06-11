package no.api.syzygy.loaders;

import no.api.syzygy.HieradirectoryHelper;
import no.api.syzygy.SyzygyException;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 *
 */
public class EmptyConfigTest {

    @Test
    public void see_what_happens_with_empty_file() {
        String readFrom = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        try {
            SyzygyLoader.loadConfigurationFile(new File(readFrom+"/emptyfile.yaml"));
            fail("If this test fails, it may be because filtering of comments in yaml file now works. " +
                         "In that case, you need to rework this test.");
        } catch ( SyzygyException se ) {
            assertTrue("Expecting specific error string, but got: "+se.getCause().getMessage(),
                       se.getCause().getMessage().startsWith("No content to map due to end-of-input"));
        }
        // You should not get a different error message out of this.
    }


}
