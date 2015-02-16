package no.api.syzygy.service;

import com.google.common.base.Strings;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import no.api.atomizer.header.BasicHeader;
import no.api.atomizer.header.HeaderManagerCreator;
import no.api.pantheon.logging.JsonLogger;
import no.api.syzygy.etcd.SynchronizationHelper;
import no.api.syzygy.service.resource.IndexPageResource;
import no.api.syzygy.service.resource.SyzygyPingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SyzygyApplication extends Application<SyzygyConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(SyzygyApplication.class);


    public static void main(String[] args) throws Exception {
        SyzygyApplication app = new SyzygyApplication();
        switch (args.length) {
            case -1:
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
            case -2:
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
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addCommand(new Cmd());
        bootstrap.addBundle(new AssetsBundle("/favicon.ico"));
    }

    @Override
    public void run(SyzygyConfiguration config, Environment environment) throws Exception {
        final HeaderManagerCreator hmcreator = new HeaderManagerCreator(config.getAtomizerHeaderConfig(),
                new BasicHeader("Access-Control-Allow-Origin", "*"),
                new BasicHeader("Access-Control-Allow-Headers", "Content-Type"));
        if (!Strings.isNullOrEmpty(config.getJsonLogPath())) {
            log.info("Logging json events to {}", config.getJsonLogPath());
            final JsonLogger jsonLogger = new JsonLogger(config.getJsonLogPath());
            jsonLogger.attach();
        }

        environment.jersey().register(new SyzygyPingResource(19087));
        environment.jersey().register(new IndexPageResource( hmcreator ));
        environment.healthChecks().register("alwayshealthy", new AlwaysHealthy());

        environment.jersey().register(new DropwizardExceptionManager(hmcreator, "/atomizer"));
    }

}