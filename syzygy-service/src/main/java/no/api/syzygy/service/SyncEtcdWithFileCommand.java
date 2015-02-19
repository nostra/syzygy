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
public class SyncEtcdWithFileCommand extends EnvironmentCommand<SyzygyConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(SyncEtcdWithFileCommand.class);

    protected SyncEtcdWithFileCommand() {
        super(new DummyApp(),"etcdsync", "Synchronize a yaml or json file to etcd");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-m","--mount").required(true)
                .help("The etcd mount point, usually the filename. It gets prefixed with /syzygy/");
        subparser.addArgument("-f", "--file").required(true)
                .help("Reference to the dropwizard configuration");
        subparser.addArgument("somefile.yml").nargs("?")
                .help("The file you want to synchronize");

// Synchronize a yaml or json file to etcd
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
        log.info("This line is on info level. Json log path: " + config.getJsonLogPath());
        log.debug("This line is on debug level");
        log.info("etcdUrl; " + config.getEtcdUrl());
        if ( config.getEtcdUrl() == null ) {
            // Need to fail explicitly, as "ignoreUnknown" kills required='true'
            //throw new SyzygyException("Missing etcd configuration");
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
