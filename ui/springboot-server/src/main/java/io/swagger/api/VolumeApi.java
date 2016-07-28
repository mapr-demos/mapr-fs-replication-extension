package io.swagger.api;

import io.swagger.model.*;

import io.swagger.model.Volume;
import io.swagger.model.Error;

import io.swagger.annotations.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.*;

@Controller
@RequestMapping(value = "/volume", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/volume", description = "the volume API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringBootServerCodegen", date = "2016-07-28T04:59:47.648Z")
public class VolumeApi {

  @ApiOperation(value = "Controls replication of a volume", notes = "Allows volume replication to be controlled", response = String.class)
  @ApiResponses(value = { 
    @ApiResponse(code = 200, message = "A response message, typically OK", response = String.class),
    @ApiResponse(code = 200, message = "Unexpected error", response = String.class) })
  @RequestMapping(value = "/{volume_name}",
    produces = { "application/json" }, 
    consumes = { "application/json" },
    method = RequestMethod.POST)
  public ResponseEntity<String> volumeVolumeNamePost(
@ApiParam(value = "The volumn name",required=true ) @PathVariable("volumeName") String volumeName

,
    

@ApiParam(value = "The configuration of the volume" ,required=true ) @RequestBody Volume volumeConfig
)
      throws NotFoundException {
      // do some magic!
      return new ResponseEntity<String>(HttpStatus.OK);
  }

}
