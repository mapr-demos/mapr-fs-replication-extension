package io.swagger.api.factories;

import io.swagger.api.VolumeApiService;
import io.swagger.api.impl.VolumeApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-07-28T04:59:33.824Z")
public class VolumeApiServiceFactory {

   private final static VolumeApiService service = new VolumeApiServiceImpl();

   public static VolumeApiService getVolumeApi()
   {
      return service;
   }
}
