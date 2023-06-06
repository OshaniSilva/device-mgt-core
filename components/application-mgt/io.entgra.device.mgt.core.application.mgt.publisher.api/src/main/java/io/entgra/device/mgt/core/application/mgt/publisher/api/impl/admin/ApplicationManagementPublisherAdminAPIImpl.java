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
package io.entgra.device.mgt.core.application.mgt.publisher.api.impl.admin;

import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.application.mgt.core.exception.BadRequestException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ForbiddenException;
import io.entgra.device.mgt.core.application.mgt.core.exception.NotFoundException;
import io.entgra.device.mgt.core.application.mgt.core.util.APIUtil;
import io.entgra.device.mgt.core.application.mgt.publisher.api.admin.ApplicationManagementPublisherAdminAPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/admin/applications")
public class ApplicationManagementPublisherAdminAPIImpl implements ApplicationManagementPublisherAdminAPI {

    private static final Log log = LogFactory.getLog(ApplicationManagementPublisherAdminAPIImpl.class);

        @DELETE
        @Consumes(MediaType.WILDCARD)
        @Path("/release/{uuid}")
        public Response deleteApplicationRelease(
                @PathParam("uuid") String releaseUuid) {
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            try {
                applicationManager.deleteApplicationRelease(releaseUuid);
                return Response.status(Response.Status.OK)
                        .entity("Successfully deleted the application release for uuid: " + releaseUuid).build();
            } catch (NotFoundException e) {
                String msg =
                        "Couldn't found application release which is having application release UUID:" + releaseUuid;
                log.error(msg, e);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            } catch (ForbiddenException e) {
                String msg = "You don't have require permission to delete the application release which has UUID "
                        + releaseUuid;
                log.error(msg, e);
                return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
            } catch (ApplicationManagementException e) {
                String msg = "Error occurred while deleting the application release for application release UUID:: "
                        + releaseUuid;
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Path("/{appId}")
    public Response deleteApplication(
            @PathParam("appId") int applicationId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteApplication(applicationId);
            return Response.status(Response.Status.OK)
                    .entity("Successfully deleted the application which has ID: " + applicationId).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found application release which is having the ID:" + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to delete the application which has ID: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application which has application ID:: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Consumes(MediaType.WILDCARD)
    @Path("/tags")
    public Response deleteTag(
            @QueryParam("tag-name") String tagName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteTag(tagName);
            return Response.status(Response.Status.OK).entity("Tag " + tagName + " is deleted successfully.").build();
        } catch (NotFoundException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while deleting registered tag.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    @Consumes("application/json")
    @Path("/categories")
    public Response addCategories(
            List<String> categoryNames) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            List<String> categories = applicationManager.addCategories(categoryNames);
            return Response.status(Response.Status.OK).entity(categories).build();
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while adding new categories.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    @Consumes("application/json")
    @Path("/categories/rename")
    public Response renameCategory(
            @QueryParam("from") String oldCategoryName,
            @QueryParam("to") String newCategoryName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.updateCategory(oldCategoryName, newCategoryName);
            return Response.status(Response.Status.OK)
                    .entity("Category is updated from " + oldCategoryName + " to " + newCategoryName).build();
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while rename registered category.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Consumes(MediaType.WILDCARD)
    @Path("/categories")
    public Response deleteCategory(
            @QueryParam("category-name") String categoryName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteCategory(categoryName);
            return Response.status(Response.Status.OK).entity("Category " + categoryName + " is deleted successfully.")
                    .build();
        } catch (NotFoundException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while deleting registered category.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/retire/{appId}")
    public Response retireApplication(
            @PathParam("appId") int applicationId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.retireApplication(applicationId);
            return Response.status(Response.Status.OK)
                    .entity("Successfully retired the application which has application ID: " + applicationId).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found application for application id: " + applicationId + " to delete the "
                    + "application";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to delete the application which has ID " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
