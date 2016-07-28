package io.swagger.api;

import io.swagger.api.*;
import io.swagger.model.*;


import io.swagger.model.Cluster;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaResteasyServerCodegen", date = "2016-07-28T04:59:59.264Z")
public abstract class ClustersApiService {
      public abstract Response clustersGet(SecurityContext securityContext)
      throws NotFoundException;
}
