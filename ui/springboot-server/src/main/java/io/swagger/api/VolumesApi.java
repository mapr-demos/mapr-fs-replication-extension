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
@RequestMapping(value = "/volumes", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/volumes", description = "the volumes API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringBootServerCodegen", date = "2016-07-28T04:59:47.648Z")
public class VolumesApi {

  @ApiOperation(value = "Currently replicating volumes", notes = "The volumes that this node is currently replicating", response = Volume.class, responseContainer = "List")
  @ApiResponses(value = { 
    @ApiResponse(code = 200, message = "An array of volumes", response = Volume.class),
    @ApiResponse(code = 200, message = "Unexpected error", response = Volume.class) })
  @RequestMapping(value = "",
    produces = { "application/json" }, 
    consumes = { "application/json" },
    method = RequestMethod.GET)
  public ResponseEntity<List<Volume>> volumesGet()
      throws NotFoundException {
      // do some magic!
      return new ResponseEntity<List<Volume>>(HttpStatus.OK);
  }

}
