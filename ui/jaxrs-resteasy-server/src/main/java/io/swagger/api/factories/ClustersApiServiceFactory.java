package io.swagger.api.factories;

import io.swagger.api.ClustersApiService;
import io.swagger.api.impl.ClustersApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaResteasyServerCodegen", date = "2016-07-28T04:59:59.264Z")
public class ClustersApiServiceFactory {

   private final static ClustersApiService service = new ClustersApiServiceImpl();

   public static ClustersApiService getClustersApi()
   {
      return service;
   }
}
