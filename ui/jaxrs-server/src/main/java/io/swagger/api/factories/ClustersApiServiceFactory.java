package io.swagger.api.factories;

import io.swagger.api.ClustersApiService;
import io.swagger.api.impl.ClustersApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-07-28T04:59:33.824Z")
public class ClustersApiServiceFactory {

   private final static ClustersApiService service = new ClustersApiServiceImpl();

   public static ClustersApiService getClustersApi()
   {
      return service;
   }
}
