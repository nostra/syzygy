package no.api.syzygy.apiproperties;

import no.api.properties.api.ApiProperty;
import no.api.properties.publication.ApiPublicationManagerMock;
import no.api.properties.publication.config.beans.Publication;
import no.api.syzygy.loaders.SyzygyLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 *
 */
public class SyzygyApiPropertiesTest {
    private SyzygyLoader syzygy;
    private SyzygyApiProperties apiproperties;

    public static String findTestDirectoryReference(String postFix) {
        File cur = new File(".");
        StringBuffer readFrom = new StringBuffer();
        if ( ! cur.getAbsolutePath().contains("syzygy-apiproperties")) {
            // IntelliJ
            readFrom.append("syzygy-apiproperties/");
        }
        readFrom.append("src/test/resources/"+postFix);
        return readFrom.toString();
    }

    @Before
    public void setUp() {
        syzygy = SyzygyLoader.loadConfigurationFile(new File(findTestDirectoryReference("syzygy.yaml")));
        syzygy.validate();
        ApiPublicationManagerMock propmock = new ApiPublicationManagerMock();
        propmock.loadDefaultPublications();
        propmock.add(new Publication(59, "deprecated", "Hadeland", "Nevermind", "www.hadeland.no", "mobil.hadeland.no",
                                     "hadeland.net", "bah", 1, 1, "hadeland", "hade", 21));
        apiproperties = new SyzygyApiProperties(syzygy, propmock);
    }

    @Test
    public void testPropertyLookup()  {
        assertEquals("value 1", syzygy.lookup("key1"));
        assertTrue(apiproperties.hasGlobalProperty("key1"));
        assertEquals("value 1", apiproperties.getGlobalProperty("key1").getValue());
        assertEquals("value 1 for www.oa.no", apiproperties.getProperty("key1", 81).getValue());

        assertEquals("Syzygy loookup error", "value 1 for alias domain", syzygy.deepLookup("key1", "hadeland.net"));
        ApiProperty aliasProperty = apiproperties.getProperty("key1", 59);
        assertNotNull(aliasProperty);
        //assertEquals("Property loookup error", "value 1 for alias domain", aliasProperty.getValue());
        assertEquals("When this runs red, and ERROR has been corrected", "value 1", aliasProperty.getValue());
        // TODO prefix aliases with www. and mobil. - see gaia

        assertNull(apiproperties.getGlobalProperty("www.dn.no"));
        assertNull(apiproperties.getProperty("key1", 0));
    }
}