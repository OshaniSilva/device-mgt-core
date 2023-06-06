/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.application.mgt.publisher.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.application.mgt.common.ApplicationList;
import io.entgra.device.mgt.core.application.mgt.common.ErrorResponse;
import io.entgra.device.mgt.core.application.mgt.common.Filter;
import io.entgra.device.mgt.core.application.mgt.common.LifecycleChanger;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.response.ApplicationRelease;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.*;
import io.entgra.device.mgt.core.device.mgt.common.Base64File;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * APIs to handle application management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Application Management Publisher Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationManagementPublisherService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-publisher/v1.0/applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "App publisher related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get ApplicationDTO Details",
                        description = "Get application details",
                        key = "perm:app:publisher:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/publisher/application/view"}
                ),
                @Scope(
                        name = "Update an ApplicationDTO",
                        description = "Update an application",
                        key = "perm:app:publisher:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/publisher/application/update"}
                )
        }
)
@Path("/applications")
@Api(value = "ApplicationDTO Management")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationManagementPublisherAPI {

    String SCOPE = "scope";

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all applications",
            notes = "This will get all applications",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got application list.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Application retrieving request payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            }) Response getApplications(
                    @ApiParam(
                            name = "Filter",
                            value = "Get application filter",
                            required = true)
                    @Valid Filter filter);

    @GET
    @Path("/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get the application of requesting application id and  state",
            notes = "This will get the application identified by the application id and state, if exists",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved relevant application.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 403,
                            message = "Forbidden. \n Don't have permission to access the application"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Application not found"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n Couldn't find an active application"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting relevant application.",
                            response = ErrorResponse.class)
            })
    Response getApplication(
            @ApiParam(
                    name = "appId",
                    value = "application Id",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "state",
                    value = "state")
            @QueryParam("state") String state
    );

    @GET
    @Path("/release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get the application release of requesting application UUID and state",
            notes = "This will get the application release identified by the application release uuid and state.",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved relevant application release.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 403,
                            message = "Forbidden. \n Don't have permission to access the application release"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Application release not found"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n Application release is in the end state of lifecycle flow"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting relevant application release.",
                            response = ErrorResponse.class)
            })
    Response getApplicationByUUID(
            @ApiParam(
                    name = "uuid",
                    value = "application release uuid",
                    required = true)
            @PathParam("uuid") String uuid
    );

    @PUT
    @Path("/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Edit an application",
            notes = "This will edit the new application",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully edited the application.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while editing the application.",
                            response = ErrorResponse.class)
            })
    Response updateApplication(
            @ApiParam(
                    name = "appId",
                    value = "application Id",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "application",
                    value = "Application data that need to be edited.",
                    required = true)
            @Valid ApplicationUpdateWrapper applicationUpdateWrapper
    );

    @POST
    @Path("/ent-app")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createEntApp(
            @ApiParam(
                    name = "application",
                    value = "The application that need to be created.",
                    required = true)
            ApplicationWrapper application,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished);

    @POST
    @Path("/web-app")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an web app",
            notes = "This will create a new web app",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created a web application.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Web app creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the web app.",
                            response = ErrorResponse.class)
            })
    Response createWebApp(
            @ApiParam(
                    name = "webapp",
                    value = "The web app that need to be created.",
                    required = true)
            WebAppWrapper webAppWrapper,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished
    );

    @POST
    @Path("/public-app")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an public app",
            notes = "This will create a new public app",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created a public app.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "public app creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the public app.",
                            response = ErrorResponse.class)
            })
    Response createPubApp(
            @ApiParam(
                    name = "public-app",
                    value = "The public app that need to be created.",
                    required = true)
            PublicAppWrapper publicAppWrapper,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished
    );

    @POST
    @Path("/custom-app")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an custom application",
            notes = "This will create a new custom application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            },
            nickname = "createCustomAppByAppWrapper"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createCustomApp(
            @ApiParam(
                    name = "application",
                    value = "The application that need to be created.",
                    required = true)
            CustomAppWrapper customAppWrapper,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{deviceType}/ent-app/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createEntAppRelease(
            @ApiParam(
                    name = "deviceType",
                    value = "Device type that application is compatible with.",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "appId",
                    value = "Id of the application.",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "applicationRelease",
                    value = "The application release that need to be created.",
                    required = true)
            EntAppReleaseWrapper entAppReleaseWrapper,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{deviceType}/public-app/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new public application release",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createPubAppRelease(
            @ApiParam(
                    name = "deviceType",
                    value = "Device type that application is compatible with.",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "appId",
                    value = "Id of the application.",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "applicationRelease",
                    value = "The application release that need to be created.",
                    required = true)
            PublicAppReleaseWrapper publicAppReleaseWrapper,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/web-app/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new web application release",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createWebAppRelease(
            @ApiParam(
                    name = "appId",
                    value = "Id of the application.",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "applicationRelease",
                    value = "The application release that need to be created.",
                    required = true)
            WebAppReleaseWrapper webAppReleaseWrapper,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{deviceType}/custom-app/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new custom application release",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createCustomAppRelease(
            @ApiParam(
                    name = "deviceType",
                    value = "Device type that application is compatible with.",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "appId",
                    value = "Id of the application.",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "applicationRelease",
                    value = "The application release that need to be created.",
                    required = true)
            CustomAppReleaseWrapper customAppReleaseWrapper,
            @ApiParam(
                    name = "isPublished",
                    value = "Published state of the application"
            )
            @QueryParam("is-published") boolean isPublished
    );

    @PUT
    @Path("/image-artifacts/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated artifacts."),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Error occurred while updating the application."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationImageArtifacts(
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "icon",
                    value = "Icon of the uploading application")
                    Base64File iconFile,
            @ApiParam(
                    name = "banner",
                    value = "Banner of the uploading application")
                    Base64File bannerFile,
            @ApiParam(
                    name = "screenshot1",
                    value = "Screen Shots of the uploading application")
                    List<Base64File> screenshots
    );

    @GET
    @Path("/device-type/{deviceType}/app-name")
    @ApiOperation(
            httpMethod = "GET",
            value = "Check the application existence",
            notes = "This API is responsible to check whether application exist or not for the given device type and "
                    + "application name.",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Application doesn't exists."),
                    @ApiResponse(
                            code = 409,
                            message = "CONFLICT. \n Application exists"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Found invalid device type with the request."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while checking the application existence"
                                    + " for given application name and device type name the application list.",
                            response = ErrorResponse.class)
            })
    Response isExistingApplication(
            @ApiParam(
                    name = "deviceType",
                    value = "Application compatible device type name",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "appName",
                    value = "Application name",
                    required = true)
            @QueryParam("appName") String appName
    );

    @PUT
    @Path("/ent-app-artifact/{deviceType}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            },
            nickname = "updateApplicationArtifactByDeviceTypeAndUUID"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully uploaded artifacts."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO artifact updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Couldn't found application/application release to update "
                                    + "application release artifact."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationArtifact(
            @ApiParam(
                    name = "deviceType",
                    value = "Type of the device i.e Android, IOS etc",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            Base64File binaryFile
    );

    @PUT
    @Path("/ent-app-release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update an application release",
            notes = "This will update a new application release",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application release.",
                            response = ApplicationReleaseDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO release updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response updateEntAppRelease(
            @ApiParam(
                    name = "uuid",
                    value = "Unique identifier of the ApplicationDTO Release",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "entAppReleaseWrapper",
                    value = "Application release wrapper which is going to update.",
                    required = true)
                    EntAppReleaseWrapper entAppReleaseWrapper);

    @PUT
    @Path("/public-app-release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update an public app release",
            notes = "This will update the public app release",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully update an app release.",
                            response = ApplicationReleaseDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Public app release updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response updatePubAppRelease(
            @ApiParam(
                    name = "uuid",
                    value = "Unique identifier of the ApplicationDTO Release",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "pubAppReleaseWrapper",
                    value = "Application release wrapper which is going to update.",
                    required = true)
                    PublicAppReleaseWrapper publicAppReleaseWrapper);

    @PUT
    @Path("/web-app-release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update an public app release",
            notes = "This will update the public app release",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully update an app release.",
                            response = ApplicationReleaseDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Public app release updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response updateWebAppRelease(
            @ApiParam(
                    name = "uuid",
                    value = "Unique identifier of the ApplicationDTO Release",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "pubAppReleaseWrapper",
                    value = "Application release wrapper which is going to update.",
                    required = true)
                    WebAppReleaseWrapper webAppReleaseWrapper
    );

    @PUT
    @Path("/custom-app-release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update an custom application release",
            notes = "This will update a custom app release",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application release.",
                            response = ApplicationReleaseDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO release updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response updateCustomAppRelease(
            @ApiParam(
                    name = "uuid",
                    value = "Unique identifier of the ApplicationDTO Release",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "entAppReleaseWrapper",
                    value = "Application release wrapper which is going to update.",
                    required = true)
                    CustomAppReleaseWrapper customAppReleaseWrapper);

    @GET
    @Path("/life-cycle/state-changes/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get lifecycle states",
            notes = "Get all lifecycle states",
            tags = "Lifecycle Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved lifecycle states.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Couldn't found an application release for application release UUID."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the lifecycle list.",
                            response = ErrorResponse.class)
            })
    Response getLifecycleStates(
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application release.")
            @PathParam("uuid") String applicationUuid);

    @POST
    @Path("/life-cycle/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a lifecycle state",
            notes = "This will add a new lifecycle state",
            tags = "Lifecycle Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully add a lifecycle state.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Lifecycle State changing request contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to move the lifecycle state of a given application release"
                                    + " to the given lifecycle state."),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Error occurred while adding new lifecycle state.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred adding a lifecycle state.",
                            response = ErrorResponse.class)
            })
    Response addLifecycleState(
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the ApplicationDTO Release",
                    required = true)
            @PathParam("uuid") String applicationUuid,
            @ApiParam(
                    name = "LifecycleChanger",
                    value = "Lifecycle Changer which contains the action and the reason for the lifecycle change.",
                    required = true)
            @Valid LifecycleChanger lifecycleChanger
    );

    @GET
    @Path("/lifecycle-config")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get application management UI configuration",
            notes = "This will get all UI configuration of application management",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got Lifecycle Config.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the lifecycle config.",
                            response = ErrorResponse.class)
            })
    Response getLifecycleConfig();

    @GET
    @Path("/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get registered application tags",
            notes = "This will get registered application tags",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got Application tags.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting application tags.",
                            response = ErrorResponse.class)
            })
    Response getTags();

    @DELETE
    @Path("/{appId}/tags/{tagName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get registered application tags",
            notes = "This will get registered application tags",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully delete  Application tags.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Given tag is not an associated tag for the given application."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting application tags.",
                            response = ErrorResponse.class)
            })
    Response deleteApplicationTag(
            @ApiParam(
                    name = "appId",
                    value = "ID of the Application",
                    required = true)
            @PathParam("appId") int applicationId,
            @ApiParam(
                    name = "tagName",
                    value = "Tag Name",
                    required = true)
            @PathParam("tagName") String tagName
    );

    @DELETE
    @Path("/tags/{tagName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete application tag",
            notes = "This will delete application tag",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully delete  registered tag.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to delete the application tag."),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Couldn't found a tag for the given tag name.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting registered tag.",
                            response = ErrorResponse.class)
            })
    Response deleteUnusedTag(
            @ApiParam(
                    name = "tagName",
                    value = "Tag Name",
                    required = true)
            @PathParam("tagName") String tagName
    );

    @PUT
    @Path("/tags/rename")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "update an application tag",
            notes = "This will update application tag",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully update the registered tag.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Request contains unaccepted values for query parameters."),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Couldn't found a tag for the given tag name.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while updating registered tag.",
                            response = ErrorResponse.class)
            })
    Response modifyTagName(
            @ApiParam(
                    name = "oldTagName",
                    value = "Existing Tag Name",
                    required = true)
            @QueryParam("from") String oldTagName,
            @ApiParam(
                    name = "newTagName",
                    value = "Modifying Tag Name",
                    required = true)
            @QueryParam("to") String newTagName
    );

    @POST
    @Path("/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add new tags.",
            notes = "This will add new tags for the system",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully add tags.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Tag adding request contains unacceptable payload."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while adding new tags.",
                            response = ErrorResponse.class)
            })
    Response addTags(
            @ApiParam(
                    name = "oldTagName",
                    value = "Existing Tag Name",
                    required = true)
                    List<String> tagNames
    );

    @POST
    @Path("/{appId}/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add new application tags",
            notes = "This will add new application tags",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully add application tags.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Application tag adding request contains unacceptable payload."),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Couldn't found an application for the given application id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while adding new application tags.",
                            response = ErrorResponse.class)
            })
    Response addApplicationTags(
            @ApiParam(
                    name = "appId",
                    value = "Existing Tag Name",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "appId",
                    value = "application Id",
                    required = true)
                    List<String> tagNames
    );

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get registered application categories",
            notes = "This will get registered application categories.",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got application categories.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting application categories.",
                            response = ErrorResponse.class)
            })
    Response getCategories();

}
