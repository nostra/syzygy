package no.api.syzygy;

import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class LoadFilesWithRelativePathTest {

    @Test
    public void testLoadWithRelativePath() {
        String basedir = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(basedir + File.separator + "withpath.yaml"));
        syzygy.validate();
        assertEquals("Key1 in first", syzygy.lookup("key1"));
        assertEquals("from common", syzygy.lookup("ending"));
    }

    @Test
    public void testLoadWithDatadirADifferentPlace() {
        String basedir = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
        SyzygyLoader syzygy = SyzygyLoader.loadConfigurationFile(new File(basedir + File.separator + "datadirpath.yaml"));
        syzygy.validate();
        assertEquals("Key1 in first", syzygy.lookup("key1"));
        assertEquals("from common", syzygy.lookup("ending"));
    }


}
