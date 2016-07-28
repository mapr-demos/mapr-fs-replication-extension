package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.VolumeApiService;
import io.swagger.api.factories.VolumeApiServiceFactory;

import io.swagger.model.Volume;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/volume")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaResteasyServerCodegen", date = "2016-07-28T04:59:59.264Z")
public class VolumeApi  {
   private final VolumeApiService delegate = VolumeApiServiceFactory.getVolumeApi();

    @POST
    @Path("/{volume_name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response volumeVolumeNamePost( @PathParam("volume_name") String volumeName, Volume volumeConfig,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.volumeVolumeNamePost(volumeName,volumeConfig,securityContext);
    }
}
