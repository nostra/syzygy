package no.api.syzygy.service;

import com.google.common.base.Strings;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import no.api.atomizer.header.BasicHeader;
import no.api.atomizer.header.HeaderManagerCreator;
import no.api.gaia.client.GaiaClient;
import no.api.pantheon.dropwizard.metrics.GraphiteReporterBuilder;
import no.api.pantheon.logging.JsonLogger;
import no.api.syzygy.etcd.EtcdConnector;
import no.api.syzygy.service.admin.CheckEtcd;
import no.api.syzygy.service.resource.IndexPageResource;
import no.api.syzygy.service.resource.SyzygyPingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyzygyApplication extends Application<SyzygyConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(SyzygyApplication.class);


    public static void main(String[] args) throws Exception {
        new SyzygyApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<SyzygyConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addCommand(new SyncEtcdWithFileCommand());
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
        GaiaClient gaia = null;
        if ( config.getGaiaURL() != null ) {
            gaia = new GaiaClient(config.getGaiaURL());
        }


        EtcdConnector etcd = EtcdConnector.attach(config.getEtcdUrl(), config.getEtcdPrefix());
        etcd.start();

        environment.jersey().register(new SyzygyPingResource(19087));
        environment.jersey().register(new IndexPageResource( hmcreator ));
        environment.healthChecks().register("etcdCheck", new CheckEtcd(etcd, environment.metrics()));

        environment.jersey().register(new DropwizardExceptionManager(hmcreator, "/atomizer"));

        if ( gaia != null ) {
            GraphiteReporterBuilder.createFor(environment.metrics())
                    .active(gaia.getConfigProperty("graphite.active"))
                    .hostName(gaia.getConfigProperty("graphite.host.name"))
                    .port(gaia.getConfigProperty("graphite.port"))
                    .startScheduledReporterFor("syzygy");
        }

    }

}