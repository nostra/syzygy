package no.api.syzygy.loaders;

import no.api.syzygy.HieradirectoryHelper;
import no.api.syzygy.SyzygyConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SyzygyConfigTest {
    private String basedir;

    @Before
    public void setUp() {
        basedir = HieradirectoryHelper.findTestDirectoryReference("yamlonly");
    }

    @Test
    public void testEquality() throws IOException {
        SyzygyConfig hyc_1 = new SyzygyFileConfig("same name").load(basedir+ File.separator+ "hieradata" +File.separator+"common.yaml");
        SyzygyConfig hyc_2 = new SyzygyFileConfig("same name").load(basedir+ File.separator+ "hieradata" +File.separator+"common.yaml");
        assertEquals("When reading the same config file, they should be equal", hyc_1, hyc_2);
        hyc_2 = new SyzygyFileConfig("same name").load(basedir+ File.separator+ "hieradata" +File.separator+"common_similar.yaml");
        assertNotEquals("When having different contents, the config sets are different.", hyc_1, hyc_2);
    }
}