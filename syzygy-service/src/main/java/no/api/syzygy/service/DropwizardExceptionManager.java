package no.api.syzygy.service;

import com.codahale.metrics.annotation.Metered;
import no.api.atomizer.header.HeaderManager;
import no.api.atomizer.header.HeaderManagerCreator;
import no.api.atomizer.header.dw.Jersey1xHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
* @deprecated Can be replaced with class from atomizer-header-dw when that package is in 1.0.3
*/
@Deprecated
class DropwizardExceptionManager implements ExceptionMapper<WebApplicationException> {
    private static final Logger log = LoggerFactory.getLogger(DropwizardExceptionManager.class);

    private final HeaderManagerCreator hmcreator;
    private final String appNameWithSlash;

    public DropwizardExceptionManager(HeaderManagerCreator hmcreator, String appNameWithSlash) {
        this.hmcreator = hmcreator;
        this.appNameWithSlash = appNameWithSlash;
    }

    @Metered
    @Override
    public Response toResponse(WebApplicationException e) {
        log.error("Caught exception in toplevel error handler:", e);
        HeaderManager headers = hmcreator.createWithNoCache();
        headers.addLocalGroup(appNameWithSlash);
        headers.addLocalGroup("/" + e.getResponse().getStatus());
        return Jersey1xHelper
                .addHeadersFromContainer(Response.status(e.getResponse().getStatus()).entity(e), headers)
                .build();
    }
}
