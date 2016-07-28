package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.ClusterApiService;
import io.swagger.api.factories.ClusterApiServiceFactory;

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

@Path("/cluster")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the cluster API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-07-28T04:59:33.824Z")
public class ClusterApi  {
   private final ClusterApiService delegate = ClusterApiServiceFactory.getClusterApi();

    @GET
    @Path("/{cluster_name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns a single cluster", notes = "The details about a single cluster", response = Cluster.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Data for this cluster", response = Cluster.class),
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = Cluster.class) })
    public Response clusterClusterNameGet(
        @ApiParam(value = "The name of the cluster to fetch",required=true) @PathParam("cluster_name") String clusterName,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.clusterClusterNameGet(clusterName,securityContext);
    }
}
