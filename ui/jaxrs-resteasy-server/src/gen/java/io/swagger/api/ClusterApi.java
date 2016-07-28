package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.ClusterApiService;
import io.swagger.api.factories.ClusterApiServiceFactory;

import io.swagger.model.Cluster;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/cluster")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaResteasyServerCodegen", date = "2016-07-28T04:59:59.264Z")
public class ClusterApi  {
   private final ClusterApiService delegate = ClusterApiServiceFactory.getClusterApi();

    @GET
    @Path("/{cluster_name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response clusterClusterNameGet( @PathParam("cluster_name") String clusterName,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.clusterClusterNameGet(clusterName,securityContext);
    }
}
