package com.mapr.fs.api;

import com.mapr.fs.ClusterDAO;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/clusters")
public class ClusterAPI  {

    private static final Logger log = Logger.getLogger(ClusterAPI.class);

    @GET
    @Produces(APPLICATION_JSON)
    public Response getClusters() throws IOException {
        log.info("==================getClusters==================");
        return Response.ok().entity(new ClusterDAO().getClusters()).build();
    }

    @GET
    @Path("/{cluster_name}")
    public Response getClusterInfo(
            @QueryParam("cluster_name") String name){

        log.info("==================addCluster==================");
        log.info("name : " + name);
        if (name == null) throw new IllegalArgumentException("cluster_name == null");

        return Response.ok().entity(new ClusterDAO().getClusterInfo(name)).build();
    }
}
