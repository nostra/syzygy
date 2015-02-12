package no.api.syzygy.service;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import no.api.syzygy.etcd.SynchronizationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SyzygyApplication extends Application<SyzygyConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(SyzygyApplication.class);


    public static void main(String[] args) throws Exception {
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
                    System.exit(1); // NOSONAR: Yes, we want to get an exit code when error.
                }
                break;
            case 0:
                app.instructions();
            default:
                new SyzygyApplication().run(args);
        }
    }

    private void instructions() {
        System.out.println("You need to supply 3 arguments: "); // NOSONAR
        System.out.println("  path/to/file/to/transfer.yaml");  // NOSONAR
        System.out.println("  target URL for etcd");  // NOSONAR
        System.out.println("  etcd mount point, usually the filename. It gets prefixed with /syzygy/"); // NOSONAR
        System.out.println(""); // NOSONAR
        System.out.println("Example 1:");  // NOSONAR
        System.out.println("   path/to/config.yaml ");        // NOSONAR
        System.out.println("   http://127.0.0.1:4001/v2/ ");  // NOSONAR
        System.out.println("   config");                      // NOSONAR
        System.out.println(""); // NOSONAR
        System.out.println("Example:");    // NOSONAR
        System.out.println("   newspaper/specific.yaml "); // NOSONAR
        System.out.println("   http://127.0.0.1:4001/v2/ "); // NOSONAR
        System.out.println("   www.rb.no/default"); // NOSONAR
    }

    @Override
    public void initialize(Bootstrap<SyzygyConfiguration> bootstrap) {

    }

    @Override
    public void run(SyzygyConfiguration configuration, Environment environment) throws Exception {

    }

}