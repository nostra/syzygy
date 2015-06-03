package no.api.syzygy.service;

import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import no.api.gaia.client.GaiaClient;
import no.api.pantheon.dropwizard.metrics.GraphiteReporterBuilder;
import no.api.pantheon.logging.JsonLogger;
import no.api.pantheon.support.BenchmarkString;
import no.api.syzygy.SyzygyException;
import no.api.syzygy.etcd.SynchronizationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
        subparser.addArgument("-o", "--output").required(true)
                .help("Output file to copy the result to. Need to do it this way, as dw hijacks stdout");
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
        // TODO Somewhat interesting that we need gaia to start syzygy... Maybe gaiaproperties should be
        // the first to be moved?
        GaiaClient gaia = null;
        if ( config.getGaiaURL() != null ) {
            gaia = new GaiaClient(config.getGaiaURL());
        }
        if ( gaia != null ) {
            GraphiteReporterBuilder.createFor(environment.metrics())
                    .active(gaia.getConfigProperty("graphite.active"))
                    .hostName(gaia.getConfigProperty("graphite.host.name"))
                    .port(gaia.getConfigProperty("graphite.port"))
                    .startScheduledReporterFor("syzygy");
        }
        Timer.Context timer = environment.metrics().timer("syncetcd.timer").time();
        try {
            if (config.getEtcdUrl() == null) {
                // Need to fail explicitly, as "ignoreUnknown" kills required='true'
                throw new SyzygyException("Missing etcd configuration");
            }
            String file = namespace.getString("somefile.yml");
            String mount = namespace.getString("mount");
            String outputFile = namespace.getString("output");
            //log.info("namespace: "+namespace);

            long bench = System.currentTimeMillis();
            // This will make a new instance. Would be better to transfer this to running application
            List<String> result = SynchronizationHelper.performSync(file, config.getEtcdUrl(), mount);
            for (String str : result) {
                log.info(str);
            }
            log.info("Total time: " + BenchmarkString.benchmarkFromMs(System.currentTimeMillis() - bench));
            writeToOutputFile(outputFile, result);
        } finally {
            timer.stop();
        }

    }

    private void writeToOutputFile(String outputFile, List<String> result) {
        if ( outputFile == null ) {
            return;
        }
        try ( Writer writer = new BufferedWriter(new FileWriter(outputFile))) {
            result.stream().forEachOrdered((s) -> writeLine( writer, s ));
            writer.flush();
        } catch (IOException e) {
            log.error("Got exception.", e);
        }
    }

    private void writeLine(Writer writer, String line) {
        try {
            writer.write(line);
            writer.write("\n");
        } catch (IOException e) {
            throw new SyzygyException("Could not write output", e);
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
            log.trace("Dummy init");
        }
        @Override
        public void run(SyzygyConfiguration configuration, Environment environment) {
            log.trace("Dummy run");
        }
    }
}
