package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.VolumeApiService;
import io.swagger.api.factories.VolumeApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;

import io.swagger.model.Volume;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/volume")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the volume API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-07-28T04:59:33.824Z")
public class VolumeApi  {
   private final VolumeApiService delegate = VolumeApiServiceFactory.getVolumeApi();

    @POST
    @Path("/{volume_name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Controls replication of a volume", notes = "Allows volume replication to be controlled", response = String.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A response message, typically OK", response = String.class),
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = String.class) })
    public Response volumeVolumeNamePost(
        @ApiParam(value = "The volumn name",required=true) @PathParam("volume_name") String volumeName,
        @ApiParam(value = "The configuration of the volume" ,required=true) Volume volumeConfig,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.volumeVolumeNamePost(volumeName,volumeConfig,securityContext);
    }
}
