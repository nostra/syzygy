package no.api.syzygy.service.resource;

import com.codahale.metrics.annotation.Metered;
import no.api.atomizer.header.HeaderManagerCreator;
import no.api.syzygy.service.view.IndexView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Path("/syzygy/")
@Produces(MediaType.TEXT_HTML)
public class IndexPageResource {


    private final HeaderManagerCreator hmcreator;

    public IndexPageResource(HeaderManagerCreator hmcreator) {
        this.hmcreator = hmcreator;
    }


    @GET
    @Metered
    public IndexView getIndex(@Context HttpServletRequest req, @QueryParam("token") String token,
            @Context HttpServletResponse response ) {
        hmcreator.createWithNoCache().addHeadersTo(response);
        return new IndexView();
    }
}
