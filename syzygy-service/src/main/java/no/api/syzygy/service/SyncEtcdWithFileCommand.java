package no.api.syzygy.service;

import com.google.common.base.Strings;
import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import no.api.pantheon.logging.JsonLogger;
import no.api.syzygy.SyzygyException;
import no.api.syzygy.etcd.SynchronizationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    }

    /**
     * Start with:
     * <pre>
     * alias file_to_etcd='rm -f *-service/target/original* ; java -jar *-service/target/*-service-*-SNAPSHOT.jar'
     * file_to_etcd etcdsync -m demo -f syzygy-app/src/main/resources/etc/dropwizard.yml ~/scrap/demo.yaml
     * </pre>
     */
    @Override
    protected void run(Environment environment, Namespace namespace, SyzygyConfiguration config) {
        if (!Strings.isNullOrEmpty(config.getJsonLogPath())) {
            final JsonLogger jsonLogger = new JsonLogger(config.getJsonLogPath());
            jsonLogger.attach();
        }

        if ( config.getEtcdUrl() == null ) {
            // Need to fail explicitly, as "ignoreUnknown" kills required='true'
            throw new SyzygyException("Missing etcd configuration");
        }

        String file = namespace.getString("somefile.yml");
        String mount = namespace.getString("mount");
        //log.info("namespace: "+namespace);

        List<String> result =  SynchronizationHelper.performSync(file, config.getEtcdUrl(), mount);
        for ( String str : result ) {
            log.info(str);
        }
    }


    /**
     * This class is only here, as the run method gets called as part of the
     * rather cryptic dropwizard command regime. Just putting the dummy here, and
     * hope the world is less obtuse in the next version.
     */
    private static class DummyApp extends Application<SyzygyConfiguration> {
        @Override
        public void initialize(Bootstrap<SyzygyConfiguration> bootstrap) {
            log.debug("Dummy init");
        }
        @Override
        public void run(SyzygyConfiguration configuration, Environment environment) {
            log.debug("Dummy run");
        }
    }
}
