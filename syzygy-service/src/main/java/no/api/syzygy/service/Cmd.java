package no.api.syzygy.service;

import com.google.common.base.Strings;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import no.api.pantheon.logging.JsonLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Cmd extends ConfiguredCommand<SyzygyConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(Cmd.class);

    protected Cmd() {
        super("etcdsync", "Synchronize a file to etcd");
    }

    @Override
    public void configure(Subparser subparser) {
        log.warn("Configure method");
        subparser.addArgument("-f", "--file").help("Full path to yamlfile to transfer.");
        subparser.addArgument("-t", "--target").help("etcd mount point, usually the filename. It gets prefixed with /syzygy/");
    }

    @Override
    protected void run(Bootstrap<SyzygyConfiguration> bootstrap, Namespace namespace, SyzygyConfiguration config) {
        if (!Strings.isNullOrEmpty(config.getJsonLogPath())) {
            log.info("Logging json events to {}", config.getJsonLogPath());
            final JsonLogger jsonLogger = new JsonLogger(config.getJsonLogPath());
            jsonLogger.attach();
        }
        log.warn("Running! Whee! NS:" + namespace.getString("t"));
        log.info("This line is on info level. Json log path: "+config.getJsonLogPath());
        log.debug("This line is on debug level");
    }


}
