package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.VolumesApiService;
import io.swagger.api.factories.VolumesApiServiceFactory;

import io.swagger.model.Volume;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/volumes")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaResteasyServerCodegen", date = "2016-07-28T04:59:59.264Z")
public class VolumesApi  {
   private final VolumesApiService delegate = VolumesApiServiceFactory.getVolumesApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response volumesGet(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.volumesGet(securityContext);
    }
}
