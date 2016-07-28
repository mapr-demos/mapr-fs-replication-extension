package io.swagger.api;

import io.swagger.model.*;

import io.swagger.model.Cluster;
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
@RequestMapping(value = "/cluster", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/cluster", description = "the cluster API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringBootServerCodegen", date = "2016-07-28T04:59:47.648Z")
public class ClusterApi {

  @ApiOperation(value = "Returns a single cluster", notes = "The details about a single cluster", response = Cluster.class)
  @ApiResponses(value = { 
    @ApiResponse(code = 200, message = "Data for this cluster", response = Cluster.class),
    @ApiResponse(code = 200, message = "Unexpected error", response = Cluster.class) })
  @RequestMapping(value = "/{cluster_name}",
    produces = { "application/json" }, 
    consumes = { "application/json" },
    method = RequestMethod.GET)
  public ResponseEntity<Cluster> clusterClusterNameGet(
@ApiParam(value = "The name of the cluster to fetch",required=true ) @PathVariable("clusterName") String clusterName

)
      throws NotFoundException {
      // do some magic!
      return new ResponseEntity<Cluster>(HttpStatus.OK);
  }

}
