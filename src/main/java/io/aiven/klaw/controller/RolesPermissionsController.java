package io.aiven.klaw.controller;

import io.aiven.klaw.model.KwRolesPermissionsModel;
import io.aiven.klaw.service.RolesPermissionsControllerService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RolesPermissionsController {

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  @RequestMapping(
      value = "/getRoles",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getRoles() {
    return new ResponseEntity<>(rolesPermissionsControllerService.getRoles(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getRolesFromDb",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getRolesFromDb() {
    return new ResponseEntity<>(rolesPermissionsControllerService.getRolesFromDb(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getPermissionDescriptions",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getPermissionDescriptions() {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.getPermissionDescriptions(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getPermissions",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, List<Map<String, Boolean>>>> getPermissions() {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.getPermissions(true), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteRole")
  public ResponseEntity<Map<String, String>> deleteRole(@RequestParam("roleId") String roleId) {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.deleteRole(roleId), HttpStatus.OK);
  }

  @PostMapping(value = "/addRoleId")
  public ResponseEntity<Map<String, String>> addRoleId(@RequestParam("roleId") String roleId) {
    return new ResponseEntity<>(rolesPermissionsControllerService.addRoleId(roleId), HttpStatus.OK);
  }

  @PostMapping(value = "/updatePermissions")
  public ResponseEntity<Map<String, String>> updatePermissions(
      @RequestBody KwRolesPermissionsModel[] kwRolesPermissionsModels) {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.updatePermissions(kwRolesPermissionsModels),
        HttpStatus.OK);
  }
}
