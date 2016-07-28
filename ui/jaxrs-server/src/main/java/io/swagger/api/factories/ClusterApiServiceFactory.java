package io.swagger.api.factories;

import io.swagger.api.ClusterApiService;
import io.swagger.api.impl.ClusterApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-07-28T04:59:33.824Z")
public class ClusterApiServiceFactory {

   private final static ClusterApiService service = new ClusterApiServiceImpl();

   public static ClusterApiService getClusterApi()
   {
      return service;
   }
}
