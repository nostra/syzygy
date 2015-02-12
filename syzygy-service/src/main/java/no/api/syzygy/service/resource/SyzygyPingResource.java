package no.api.syzygy.service.resource;

import no.api.pantheon.dropwizard.admin.DropWizardPingResource;

import javax.ws.rs.Path;

/**
 *
 */
@Path("/syzygy/apiadmin/ping")
public class SyzygyPingResource extends DropWizardPingResource {

    public SyzygyPingResource(int port) {
        super(port);
    }
}
