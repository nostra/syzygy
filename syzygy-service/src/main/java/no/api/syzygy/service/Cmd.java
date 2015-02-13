package no.api.syzygy.service;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Cmd extends Command {
    private static final Logger log = LoggerFactory.getLogger(Cmd.class);

    protected Cmd() {
        super("ls", "This is just some description");
    }

    @Override
    public void configure(Subparser subparser) {
        log.debug("Configure "+subparser);
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        log.debug("Running! Whee! NS:"+namespace);
    }
}
