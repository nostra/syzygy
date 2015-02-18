package no.api.syzygy.service;

import com.google.common.base.Strings;
import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import no.api.pantheon.logging.JsonLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Cmd extends EnvironmentCommand<SyzygyConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(Cmd.class);

    protected Cmd() {
        super(new DummyApp(),"etcdsync", "Synchronize a file to etcd");
    }

    @Override
    public void configure(Subparser subparser) {
        log.warn("Configure method");
        subparser.addArgument("file").nargs("?")
                .help("Synchronize a yaml or json file to etcd");

    }

    /**
     * Start with:
     * etcdsync syzygy-app/src/main/resources/etc/dropwizard.yml
     */
    @Override
    protected void run(Environment environment, Namespace namespace, SyzygyConfiguration config) {
        if (!Strings.isNullOrEmpty(config.getJsonLogPath())) {
            log.info("Logging json events to {}", config.getJsonLogPath());
            final JsonLogger jsonLogger = new JsonLogger(config.getJsonLogPath());
            jsonLogger.attach();
        }
        log.warn("Running! Whee! NS:" + namespace.getString("file"));
        log.info("This line is on info level. Json log path: "+config.getJsonLogPath());
        log.debug("This line is on debug level");
        if ( config.getEtcdUrl() == null ) {
            // Need to fail explicitly, as "ignoreUnknown" kills required='true'
            throw new RuntimeException("Missing etcd configuration");
        }

    }


    private static class DummyApp extends Application<SyzygyConfiguration> {

        @Override
        public void initialize(Bootstrap<SyzygyConfiguration> bootstrap) {
            log.warn("Dummy init");
        }

        @Override
        public void run(SyzygyConfiguration configuration, Environment environment) throws Exception {
            log.error("Dummy run");
            log.info("This line is on info level. Json log path: " + configuration.getJsonLogPath());
        }
    }
}
