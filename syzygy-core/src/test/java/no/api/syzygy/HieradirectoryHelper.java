package no.api.syzygy;

import java.io.File;

/**
 *
 */
public final class HieradirectoryHelper {

    private HieradirectoryHelper() {
        // Intentional
    }

    public static String findTestDirectoryReference(String subdirectory) {
        File cur = new File(".");
        StringBuffer readFrom = new StringBuffer();
        if ( ! cur.getAbsolutePath().contains("syzygy-core")) {
            // IntelliJ
            readFrom.append("syzygy-core/");
        }
        readFrom.append("src/test/resources/"+subdirectory);
        return readFrom.toString();
    }
}
