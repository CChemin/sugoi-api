/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.services.controller;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.model.Application;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Tag(
    name = "Manage applications",
    description = "New endpoints to create, update, delete, or find application")
@RequestMapping(value = {"/v2", "/"})
@SecurityRequirements(
    value = {@SecurityRequirement(name = "oAuth"), @SecurityRequirement(name = "basic")})
public class ApplicationController {

  @Autowired private ApplicationService applicationService;

  @GetMapping(
      path = {"/realms/{realm}/storages/{storage}/applications"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Search applications by parameter")
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Applications found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  public ResponseEntity<PageResult<Application>> getApplications(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Expected size of result", required = false)
          @RequestParam(name = "size", defaultValue = "20")
          int size,
      @Parameter(description = "Offset to apply when searching", required = false)
          @RequestParam(name = "offset", required = false, defaultValue = "0")
          int offset,
      @Parameter(description = "Name of the app searched", required = false)
          @RequestParam(value = "name", required = false)
          String name,
      @Parameter(description = "Name of the owner of the app searched", required = false)
          @RequestParam(value = "owner", required = false)
          String owner) {
    Application applicationFilter = new Application();
    applicationFilter.setName(name);
    applicationFilter.setOwner(owner);

    PageableResult pageableResult = new PageableResult(size, offset);

    PageResult<Application> foundApplications =
        applicationService.findByProperties(realm, storage, applicationFilter, pageableResult);

    if (foundApplications.isHasMoreResult()) {
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .replaceQueryParam("offset", offset + size)
              .build()
              .toUri();
      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(foundApplications);
    } else {
      return ResponseEntity.status(HttpStatus.OK).body(foundApplications);
    }
  }

  @GetMapping(
      path = {"/realms/{realm}/applications"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Search applications by parameter")
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Applications found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  public ResponseEntity<PageResult<Application>> getApplications(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Expected size of result", required = false)
          @RequestParam(name = "size", defaultValue = "20")
          int size,
      @Parameter(description = "Offset to apply when searching", required = false)
          @RequestParam(name = "offset", required = false, defaultValue = "0")
          int offset,
      @Parameter(description = "Name of the app searched", required = false)
          @RequestParam(value = "name", required = false)
          String name,
      @Parameter(description = "Name of the owner of the app searched", required = false)
          @RequestParam(value = "owner", required = false)
          String owner) {
    return getApplications(realm, null, size, offset, name, owner);
  }

  @PostMapping(
      value = {"/realms/{realm}/storages/{storage}/applications"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Application created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Application.class))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "Application already exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<Application> createApplication(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Application to create", required = true) @RequestBody
          Application application) {

    if (applicationService.findById(realm, storage, application.getName()) == null) {
      applicationService.create(realm, storage, application);

      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + application.getName())
              .build()
              .toUri();

      return ResponseEntity.created(location)
          .body(applicationService.findById(realm, storage, application.getName()));
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PostMapping(
      value = {"/realms/{realm}/applications"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Application created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Application.class))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "Application already exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<Application> createApplication(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Application to create", required = true) @RequestBody
          Application application) {
    return createApplication(realm, null, application);
  }

  @PutMapping(
      value = {"/realms/{realm}/storages/{storage}/applications/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Update application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Application.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Application does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<Application> updateApplication(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Id of the app to update", required = true) @PathVariable("id")
          String id,
      @Parameter(description = "Application to update", required = true) @RequestBody
          Application application) {

    if (!application.getName().equals(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    if (applicationService.findById(realm, storage, id) != null) {
      applicationService.update(realm, storage, application);

      URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(applicationService.findById(realm, storage, id));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @PutMapping(
      value = {"/realms/{realm}/applications/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Update application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Application.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Application does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<Application> updateApplication(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Id of the app to update", required = true) @PathVariable("id")
          String id,
      @Parameter(description = "Application to update", required = true) @RequestBody
          Application application) {
    return updateApplication(realm, null, id, application);
  }

  @DeleteMapping(
      value = {"/realms/{realm}/storages/{storage}/applications/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Application deleted",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "Application does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<String> deleteApplication(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Application's id to delete", required = false) @PathVariable("id")
          String id) {

    if (applicationService.findById(realm, storage, id) != null) {
      applicationService.delete(realm, storage, id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @DeleteMapping(
      value = {"/realms/{realm}/applications/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Application deleted",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "Application does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<String> deleteApplication(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Application's id to delete", required = false) @PathVariable("id")
          String id) {
    return deleteApplication(realm, null, id);
  }

  @GetMapping(
      path = {"/realms/{realm}/storages/{storage}/applications/{name}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get application by name")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Application.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Application does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<Application> getApplicationByName(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Name of the app searched", required = true) @PathVariable("name")
          String name) {
    Application application = applicationService.findById(realm, storage, name);
    if (application != null) {
      return ResponseEntity.status(HttpStatus.OK).body(application);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping(
      path = {"/realms/{realm}/applications/{name}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get application by name")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Application.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Application does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<Application> getApplicationByName(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Name of the app searched", required = true) @PathVariable("name")
          String name) {
    return getApplicationByName(realm, null, name);
  }
}
