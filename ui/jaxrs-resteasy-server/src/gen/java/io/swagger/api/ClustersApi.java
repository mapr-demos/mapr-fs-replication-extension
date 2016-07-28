package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.ClustersApiService;
import io.swagger.api.factories.ClustersApiServiceFactory;

import io.swagger.model.Cluster;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/clusters")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaResteasyServerCodegen", date = "2016-07-28T04:59:59.264Z")
public class ClustersApi  {
   private final ClustersApiService delegate = ClustersApiServiceFactory.getClustersApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response clustersGet(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.clustersGet(securityContext);
    }
}
