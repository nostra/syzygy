package no.api.syzygy.etcd;

import java.io.File;

/**
 *
 */
public class DirectoryHelper {

    public static final String ETCD_URL = "http://127.0.0.1:4001/v2/";
    public static final String SYZYGY_JUNIT_PATH = "/syzygy/junit/";

    protected static String findTestResourcesDirectory() {
        File cur = new File(".");
        StringBuffer readFrom = new StringBuffer();
        if ( ! cur.getAbsolutePath().contains("syzygy-etcd")) {
            // IntelliJ
            readFrom.append("syzygy-etcd/");
        }
        readFrom.append("src/test/resources/");
        return readFrom.toString();
    }
}
