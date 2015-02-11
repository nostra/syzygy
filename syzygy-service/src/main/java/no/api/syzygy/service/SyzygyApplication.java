package no.api.syzygy.service;

import no.api.syzygy.etcd.SynchronizationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SyzygyApplication {
    private static final Logger log = LoggerFactory.getLogger(SyzygyApplication.class);
    public static void main(String[] args) {
        SyzygyApplication app = new SyzygyApplication();
        switch (args.length) {
            case 3:
                try {
                    List<String> result = SynchronizationHelper.performSync(args[0], args[1], args[2]);
                    for ( String str : result ) {
                        log.info(str);
                    }
                } catch (Exception e ) {
                    log.error("Execution ended with exception.", e);
                    System.exit(1);
                }
                break;
            default:
                app.instructions();
        }
    }

    private void instructions() {
        System.out.println("You need to supply 3 arguments: ");
        System.out.println("  path/to/file/to/transfer.yaml");
        System.out.println("  target URL for etcd");
        System.out.println("  etcd mount point, usually the filename. It gets prefixed with /syzygy/");
        System.out.println("");
        System.out.println("Example 1:");
        System.out.println("   path/to/config.yaml ");
        System.out.println("   http://127.0.0.1:4001/v2/ ");
        System.out.println("   config");
        System.out.println("");
        System.out.println("Example:");
        System.out.println("   newspaper/specific.yaml ");
        System.out.println("   http://127.0.0.1:4001/v2/ ");
        System.out.println("   www.rb.no/default");
    }


}