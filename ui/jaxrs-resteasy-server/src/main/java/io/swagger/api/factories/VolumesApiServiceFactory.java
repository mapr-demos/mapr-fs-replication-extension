package io.swagger.api.factories;

import io.swagger.api.VolumesApiService;
import io.swagger.api.impl.VolumesApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaResteasyServerCodegen", date = "2016-07-28T04:59:59.264Z")
public class VolumesApiServiceFactory {

   private final static VolumesApiService service = new VolumesApiServiceImpl();

   public static VolumesApiService getVolumesApi()
   {
      return service;
   }
}
