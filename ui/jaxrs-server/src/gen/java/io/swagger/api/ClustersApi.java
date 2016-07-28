package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.ClustersApiService;
import io.swagger.api.factories.ClustersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;

import io.swagger.model.Cluster;
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

@Path("/clusters")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the clusters API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-07-28T04:59:33.824Z")
public class ClustersApi  {
   private final ClustersApiService delegate = ClustersApiServiceFactory.getClustersApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Known clusters", notes = "The clusters endpoint returns info about the clusters that are known to the current cluster", response = Cluster.class, responseContainer = "List", tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of clusters", response = Cluster.class, responseContainer = "List"),
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = Cluster.class, responseContainer = "List") })
    public Response clustersGet(
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.clustersGet(securityContext);
    }
}
