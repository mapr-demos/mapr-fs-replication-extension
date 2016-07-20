package com.mapr.fs.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mapr.fs.ClusterDAO;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/volumes")
public class VolumeAPI {
    private static final Logger log = Logger.getLogger(VolumeAPI.class);

    @GET
    @Path("/{cluster_name}")
    @Produces(APPLICATION_JSON)
    public Response getVolumes(
            @QueryParam("cluster_name") String name) throws JsonProcessingException {
        log.info("==================getVolumes==================");

        if (name == null) throw new IllegalArgumentException("cluster_name == null");
        return Response.ok().entity(new ClusterDAO().getVolumes(name)).build();
    }

    @POST
    @Path("/{volume_name}")
    public Response addVolume(
            @QueryParam("volume_name") String vol_name,
            @QueryParam("cluster_name") String clust_name,
            @QueryParam("path") String path) throws IOException {

        log.info("==================addVolume==================");
        log.info("name : " + vol_name);

        if (vol_name == null) throw new IllegalArgumentException("vol_name == null");
        if (clust_name == null) throw new IllegalArgumentException("cluster_name == null");
        if (path == null) throw new IllegalArgumentException("path == null");

        new ClusterDAO().put(clust_name, vol_name, path);

        return Response.ok().build();
    }

}
